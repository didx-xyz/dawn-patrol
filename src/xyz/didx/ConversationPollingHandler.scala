package xyz.didx

import scala.collection.mutable
import io.circe._
import io.circe.parser.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.SttpBackend
import xyz.didx.config.ConfigReaders.*
import xyz.didx.logging.LogWriter.*
import xyz.didx.didcomm.*

import java.net.URI
import xyz.didx.signal.SignalBot
import cats.effect.kernel.Resource
import xyz.didx.connection.RedisStorage
import xyz.didx.messages.*
import cats.data.EitherT
import cats.effect.IO
import cats.implicits.*
import cats.syntax.traverse.*

import SignalMessageCodec.memberDecoder
import xyz.didx.registry.*
import RegistryResponseCodec.encodeRegistryRequest
import xyz.didx.didcomm.Service
import xyz.didx.didcomm.ServiceEndpointNodes
import xyz.didx.registry.RegistryClient
import xyz.didx.openai.OpenAIAgent
import xyz.didx.passkit.PasskitAgent
import xyz.didx.ai.AiHandler
import xyz.didx.ai.model.ChatState

class ConversationPollingHandler(using logger: Logger[IO]):
  val appConf      = getConf(using logger)
  val registryConf = getRegistryConf(using logger)
  val signalConf   = getSignalConf(using logger)

  val registryClient = RegistryClient(
    registryConf.registrarUrl.toString(),
    registryConf.apiKey
  )

  private val userStates: mutable.Map[String, ChatState] = mutable.Map()

  // val redisStorage: Resource[cats.effect.IO, RedisStorage] =
  //   RedisStorage.create(appConf.redisUrl.toString())

  def converse(
    backend: SttpBackend[IO, Any]
  ): IO[Either[Exception, List[String]]] =
    for {
      signalBot              <- IO.pure(SignalBot(backend))
      receivedMessagesEither <- signalBot
                                  .receive()
                                  .flatTap(receivedMessages => logNonEmptyList[SignalSimpleMessage](receivedMessages))
      responses              <- receivedMessagesEither match {
                                  case Right(messages) =>
                                    processMessages(messages, signalBot, backend)
                                  case Left(exception) =>
                                    IO.pure(Left(exception): Either[Exception, List[String]])
                                }
    } yield responses

  private def processMessages(
    messages: List[SignalSimpleMessage],
    signalBot: SignalBot,
    backend: SttpBackend[IO, Any]
  ): IO[Either[Exception, List[String]]] = {
    val (adminMessages, userMessages) =
      messages.filter(_.text.nonEmpty).partition(_.text.toLowerCase.startsWith("@admin|add"))

    for {
      _               <- processAdminMessages(adminMessages, signalBot, backend)
      responseResults <- getResponsesFromUserMessages(userMessages, signalBot)
    } yield responseResults
  }.value

  private def processAdminMessages(
    adminMessages: List[SignalSimpleMessage],
    signalBot: SignalBot,
    backend: SttpBackend[IO, Any]
  ): EitherT[IO, Exception, Unit] =
    adminMessages.traverse_(processAdminMessage(_, signalBot, backend))

  private def processAdminMessage(
    message: SignalSimpleMessage,
    signalBot: SignalBot,
    backend: SttpBackend[IO, Any]
  ) = for {
    member <- EitherT.fromEither[IO] {
                decode[Member](message.text.split("\\|")(2))
                  .leftMap(err => new Exception(s"Failed to decode Member: ${err.getMessage}"))
              }

    doc = DIDDoc(
            did = "",
            controller = Some(s"${appConf.dawnControllerDID}"),
            alsoKnownAs = Some(Set(s"tel:${member.number};name=${member.name}")),
            services = Some(
              Set(
                Service(
                  id = new URI("#dwn"),
                  `type` = Set("DecentralizedWebNode"),
                  serviceEndpoint = Set(
                    ServiceEndpointNodes(
                      nodes = appConf.dawnServiceUrls
                    )
                  )
                )
              )
            )
          )

    reg      = RegistryRequest(doc)
    document = reg.asJson.spaces2

    did <- EitherT(
             registryClient
               .createDID(registryConf.didMethod, document, backend)
               .map(_.leftMap(err => new Exception(s"Failed to create DID: ${err.getMessage}")))
           )

    pass <- PasskitAgent(member.name, did, appConf.dawnUrl).signPass()

    result <- EitherT(signalBot.send(
                SignalSendMessage(
                  attachments = List(
                    s"data:application/vnd.apple.pkpass;filename=did.pkpass;base64,$pass"
                  ),
                  message = s"${member.name}, ${appConf.dawnWelcomeMessage}",
                  number = signalConf.signalPhone,
                  recipients = List[String](member.number)
                )
              ))

    _ <- EitherT.liftF(logger.info(s"Sent badge with $did to : ${member.name}"))

  } yield ()

  private def getResponsesFromUserMessages(
    userMessages: List[SignalSimpleMessage],
    signalBot: SignalBot
  ): EitherT[IO, Exception, List[String]] =
    userMessages.traverse(getResponseFromUserMessage(_, signalBot))

  private def getResponseFromUserMessage(
    message: SignalSimpleMessage,
    signalBot: SignalBot
  ): EitherT[IO, Exception, String] =
    val userPhone = message.phone

    // Retrieve the current state of the user, defaulting to Onboarding if not present
    val currentState = userStates.getOrElse(userPhone, ChatState.Onboarding)

    val (response, nextState) = AiHandler.getAiResponse(
      input = message.text,
      conversationId = userPhone,
      state = currentState,
      telNo = Some(userPhone)
    )

    // Update the state map with the new state for this user
    userStates.update(userPhone, nextState)

    val signalMessage: SignalSimpleMessage = SignalSimpleMessage(userPhone, message.name, response)

    for {
      sendResult: String <- processAndRespond(signalMessage, signalBot)
    } yield sendResult

  private def processAndRespond(
    k: SignalSimpleMessage,
    signalBot: SignalBot
  ): EitherT[IO, Exception, String] =
    k match {
      case m: SignalSimpleMessage if m.text.toLowerCase().contains("https://maps.google.com") =>
        EitherT.fromEither[IO](Left(new Exception("Not implemented yet")))

      case _ =>
        EitherT(
          signalBot.send(
            SignalSendMessage(
              List[String](),
              k.text,
              signalConf.signalPhone,
              List(k.phone)
            )
          )
        )
    }