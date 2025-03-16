package xyz.didx.ai.handler

import cats.data.EitherT
import cats.effect.IO
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import sttp.client4._
import sttp.model.Header
import ujson.Value.Value
import xyz.didx.ai.embedding.EmbeddingHandler
import xyz.didx.ai.model.Opportunity

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

object Opportunities {

  val backend = DefaultFutureBackend()

  def fetchAndStoreOpportunities(): IO[Unit] =
    for {
      _    <- IO.pure(scribe.info(s"Fetching opportunities from Yoma API"))
      opps <- Opportunities.fetchOpportunities()
      _    <- IO.pure(scribe.info(s"Successfully fetched opportunities."))
    } yield EmbeddingHandler.getAndStoreAll(opps)

  def fetchOpportunities(): IO[List[Opportunity]] = {
    val request = emptyRequest
      .post(uri"https://api.yoma.world/api/v3/opportunity/search")
      .body("""{"pageNumber": 1, "pageSize": 1000}""")
      .withHeaders(Seq(
        Header("Accept", "text/plain"),
        Header("Content-Type", "application/json-patch+json")
      ))
      .response(asStringAlways)

    val decodedOpportunities: EitherT[IO, String, List[Value]] = for {
      response      <- EitherT.liftF(IO.fromFuture(IO(request.send(backend))))
      parsed        <- EitherT.fromEither(
                         Try(ujson.read(response.body)).toEither.left
                           .map(e => e.getMessage)
                       )
      opportunities <- EitherT.fromEither(
                         Try(parsed("items").arr.to[List[Value]]).toEither.left
                           .map(e => e.getMessage)
                       )
    } yield opportunities

    val listOfOpportunities: IO[List[Opportunity]] = decodedOpportunities.value.flatMap {
      case Right(decodeSuccess) =>
        decode[List[Opportunity]](decodeSuccess) match {
          case Right(opportunities) =>
            IO.pure(opportunities)
          case Left(parseError)     =>
            IO(scribe.warn(s"Opportunities parsing error: $parseError")).as(List.empty[Opportunity])
        }

      case Left(decodeError) =>
        IO(scribe.warn(s"Opportunities decoding error: $decodeError")).as(List.empty[Opportunity])
    }

    listOfOpportunities
  }

  private def decodeBase64(encoded: String): Array[Byte] =
    Base64.getDecoder.decode(encoded)

  private def decompressGzip(compressed: Array[Byte]): String = {
    val inputStream  = new GZIPInputStream(new ByteArrayInputStream(compressed))
    val outputStream = new ByteArrayOutputStream()

    val buffer = new Array[Byte](1024)
    var length = 0
    while ({ length = inputStream.read(buffer); length } > 0)
      outputStream.write(buffer, 0, length)

    new String(outputStream.toByteArray, "UTF-8")
  }
}
