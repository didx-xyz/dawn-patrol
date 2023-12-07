package xyz.didx.signal

import cats.data.EitherT
import cats.effect.FiberIO
import cats.effect.IO
import cats.effect.Ref
import cats.implicits.*
import fs2.Stream
import io.circe.syntax.*
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.generic.derivation.default.*
import sttp.client3.*
import sttp.client3.circe.asJson
import sttp.model.StatusCode
import xyz.didx.messages.SignalMessage
import xyz.didx.messages.SignalMessageCodec.signalMessageDecoder
import xyz.didx.messages.SignalMessageCodec.signalSendMessage
import xyz.didx.messages.SignalSendMessage
import xyz.didx.messages.SignalSimpleMessage

import scala.concurrent.duration._

case class SignalConfig(
  signalUrl: String,
  signalUser: String,
  signalPhone: String,
  signalTimeout: Int = 5
) derives ConfigReader:
  override def toString: String =
    s"SignalConfig(url: $signalUrl, user: $signalUser, phone: $signalPhone)"

case class SignalBot(backend: SttpBackend[IO, Any]):
  type ErrorOr[A] = EitherT[IO, Exception, A]
  def getConf(): SignalConfig =
    ConfigSource.default.at("signal-conf").load[SignalConfig] match
      case Left(error) =>
        SignalConfig("", "", "")
      case Right(conf) => conf

  val signalConf = getConf()

  def init(): Unit = ()

  def register(voiceMode: Boolean): IO[Either[Error, String]] =
    val request = basicRequest
      .contentType("application/json")
      .body(s"""{"use_voice": $voiceMode}""")
      .post(uri"${signalConf.signalUrl}/register/${signalConf.signalPhone}")

    val response = request.send(backend)

    response.map(c =>
      c.code match
        case s: StatusCode if s.isSuccess =>
          Right(s"Signalbot register: $s")
        case s: StatusCode                =>
          Left(Error(s"Signalbot register: $s"))
    )

  def verify(pin: String): IO[Either[Error, String]] =
    val request  = basicRequest
      .contentType("application/json")
      .body(s"""{"pin": $pin}""")
      .post(uri"${signalConf.signalUrl}/verify/${signalConf.signalPhone}")
    val response = request.send(backend)
    response.map(c =>
      c.code match
        case s: StatusCode if s.isSuccess =>
          Right(s"Signalbot verify: $s")
        case s: StatusCode                =>
          Left(Error(s"Signalbot verify: $s"))
    )

  def send(message: SignalSendMessage): IO[Either[Error, String]] =
    val request = basicRequest
      .contentType("application/json")
      .body(message.asJson.noSpaces)
      .post(uri"${signalConf.signalUrl}/v2/send")

    val response = request.send(backend)
    scribe.info(s"Sent message: $message")
    response.map(c =>
      c.code match
        case s: StatusCode if s.isSuccess =>
          Right(s"Signalbot Send: $s - Message sent")
        case s: StatusCode                =>
          Left(Error(s"Signalbot Send: $s"))
    )

  def receive(): IO[
    Either[Error, List[SignalSimpleMessage]]
  ] =
    val request = basicRequest
      .contentType("application/json")
      .response(asJson[List[SignalMessage]])
      .get(
        uri"${signalConf.signalUrl}/v1/receive/${signalConf.signalPhone}?timeout=${signalConf.signalTimeout}"
      )

    val response = request.send(backend)
    response map (r =>
      r.body match
        case Left(error)     => Left(Error(error.getMessage))
        case Right(messages) =>
          messages
            .map(msg =>
              msg.envelope.dataMessage.map(dm =>
                Right(
                  SignalSimpleMessage(
                    msg.envelope.sourceNumber,
                    msg.envelope.sourceName,
                    dm.message
                  )
                )
              )
            )
            .flatten
            .sequence
    )

  private def typingIndicatorTask(userNumber: String): fs2.Stream[IO, Unit] =
    // The typing indicator times out after ~15 seconds, so we will continue sending the typing indicator until complete
    Stream.eval(sendTypingIndicator(userNumber).void) ++                                   // Send typing indicator immediately
      Stream.awakeEvery[IO](10.seconds).evalMap(_ => sendTypingIndicator(userNumber).void) // And then every 10 seconds

  def startTyping(userNumber: String, taskRef: Ref[IO, Option[FiberIO[Unit]]]): IO[Unit] =
    taskRef.get.flatMap {
      case Some(fiber) => fiber.cancel
      case None        => IO.unit
    } *> typingIndicatorTask(userNumber).compile.drain.start.flatMap(fiber => taskRef.set(Some(fiber)))

  def stopTyping(userNumber: String, taskRef: Ref[IO, Option[FiberIO[Unit]]]): IO[Unit] =
    taskRef.get.flatMap {
      case Some(fiber) =>
        fiber.cancel *> sendStopTypingIndicator(userNumber).void
      case None        => IO.unit
    }

  def sendTypingIndicator(userNumber: String): IO[
    Either[Error, Unit]
  ] =
    val request = basicRequest
      .contentType("application/json")
      .body(s"""{"recipient": "$userNumber"}""")
      .put(
        uri"${signalConf.signalUrl}/v1/typing-indicator/${signalConf.signalPhone}"
      )

    val response = request.send(backend)
    scribe.debug(s"Send start typing indicator for $userNumber")
    response map (r =>
      r.body match
        case Left(error) => Left(Error(error))
        case Right(_)    => Right(IO.unit)
    )

  def sendStopTypingIndicator(userNumber: String): IO[
    Either[Error, Unit]
  ] =
    val request = basicRequest
      .contentType("application/json")
      .body(s"""{"recipient": "$userNumber"}""")
      .delete(
        uri"${signalConf.signalUrl}/v1/typing-indicator/${signalConf.signalPhone}"
      )

    val response = request.send(backend)
    scribe.debug(s"Send stop typing indicator for $userNumber")
    response map (r =>
      r.body match
        case Left(error) => Left(Error(error))
        case Right(_)    => Right(IO.unit)
    )
