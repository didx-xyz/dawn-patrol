package xyz.didx.openai

import cats.data.EitherT
import cats.effect.IO
import cats.effect.Sync
import cats.implicits._
import cats.implicits._
import xyz.didx.messages.SignalSimpleMessage
import io.circe.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.*
import pureconfig.generic.derivation.default.*
import sttp.client3.*
import sttp.client3.circe.*

case class APIConf(apiKey: String, orgId: String) derives ConfigReader:
  override def toString: String =
    s"APIConf(key: ${apiKey.toString}, orgId: ${orgId.toString})"

final case class OpenAIAgent(backend: SttpBackend[IO, Any])(using
    logger: Logger[IO]
):
  import OpenAIMessageCodec.openAIRequestEncoder
  import OpenAIMessageCodec.openAIResponseDecoder

  private val client = SimpleHttpClient()
  def log[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    logger.info(s"$value")
  def getConf() =
    val apiConf: APIConf =
      ConfigSource.default.at("openai-conf").load[APIConf] match
        case Left(error) =>
          IO(logger.error(s"$error") *> IO.pure(error))
          APIConf("", "")
        case Right(conf) => conf
    apiConf

  val apiConf = getConf()

  def extractKeywords(
      message: SignalSimpleMessage
  ): EitherT[IO, ResponseException[String, Error], SignalSimpleMessage] =
    val openAIRequest =
      OpenAIRequest(prompt = s"Extract keywords from this text: ${message.text}").asJson.noSpaces
    val request = basicRequest
      .contentType("application/json")
      .header(
        "Authorization",
        s"Bearer ${apiConf.apiKey}",
        replaceExisting = true
      )
      .body(openAIRequest)
      .response(asJson[OpenAIResponse])
      .post(uri"https://api.openai.com/v1/completions")
    // val curl = request.toCurl
    // println(s"curl: \n $curl")
    // val response = client.send(request)
    val response: IO[
      Response[Either[ResponseException[String, Error], OpenAIResponse]]
    ] = request.send(backend)
    EitherT(response.map { r =>
      r.body match
        case Left(error) =>
          response.flatTap(t => logger.error(s"Error: $error"))
          Left(error)
        case Right(openAIResponse) =>
          val keys = openAIResponse.choices.map(choice => choice.text)
          response.flatTap(t => logger.info(s"Keywords: ${keys.mkString(", ")}}}"))

          Right(message.copy(keywords = keys))
    })
    // case Right(openAIResponse) =>
    //   val keys = openAIResponse.choices.map(choice => choice.text)
    //   EitherT.right(message.copy(keywords = keys))
