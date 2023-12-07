package xyz.didx.registry

import cats.*
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.*
import io.circe.syntax.*
import io.circe.syntax._
import xyz.didx.didcomm.DIDCodec
import xyz.didx.didcomm.DIDCodec.encodeDIDDoc

object RegistryResponseCodec:
  import io.circe.generic.auto.*
  import io.circe.parser.*
  import io.circe.syntax.*
  import io.circe.{Decoder, Encoder}

  given registryResponseDecoder: Decoder[RegistryResponse] =
    new Decoder[RegistryResponse] {
      final def apply(c: HCursor): Decoder.Result[RegistryResponse] =
        for {
          jobId                   <- c.downField("jobId").as[Option[String]]
          didState                <- c.downField("didState").as[DIDState]
          didRegistrationMetadata <- c
                                       .downField("didRegistrationMetadata")
                                       .as[DIDRegistrationMetadata]
          didDocumentMetadata     <- c
                                       .downField("didDocumentMetadata")
                                       .as[DIDDocumentMetadata]
        } yield RegistryResponse(
          jobId,
          didState,
          didRegistrationMetadata,
          didDocumentMetadata
        )
    }

  given Decoder[LedgerResult] = new Decoder[LedgerResult] {
    final def apply(c: HCursor): Decoder.Result[LedgerResult] =
      for {
        result <- c.downField("result").as[Result]
      } yield LedgerResult(result)
  }

  given Decoder[Result] = new Decoder[Result] {
    final def apply(c: HCursor): Decoder.Result[Result] =
      for {
        reqSignature <- c.downField("reqSignature").as[ReqSignature]
        txn          <- c.downField("txn").as[Txn]
        rootHash     <- c.downField("rootHash").as[String]
        ver          <- c.downField("ver").as[String]
      } yield Result(reqSignature, txn, rootHash, ver)
  }

  given Decoder[ReqSignature] = new Decoder[ReqSignature] {
    final def apply(c: HCursor): Decoder.Result[ReqSignature] =
      for {
        `type` <- c.downField("type").as[String]
        values <- c.downField("values").as[List[Values]]
      } yield ReqSignature(`type`, values)
  }

  given Decoder[Values] = new Decoder[Values] {
    final def apply(c: HCursor): Decoder.Result[Values] =
      for {
        value <- c.downField("value").as[String]
        from  <- c.downField("from").as[String]
      } yield Values(value, from)
  }

  given Decoder[Txn] = new Decoder[Txn] {
    final def apply(c: HCursor): Decoder.Result[Txn] =
      for {
        data            <- c.downField("data").as[Data]
        `type`          <- c.downField("type").as[String]
        protocolVersion <- c.downField("protocolVersion").as[Int]
        metadata        <- c.downField("metadata").as[Metadata]
      } yield Txn(data, `type`, protocolVersion, metadata)
  }

  given Decoder[Data] = new Decoder[Data] {
    final def apply(c: HCursor): Decoder.Result[Data] =
      for {
        raw  <- c.downField("raw").as[String]
        dest <- c.downField("dest").as[String]
      } yield Data(raw, dest)
  }

  given Decoder[Metadata] = new Decoder[Metadata] {
    final def apply(c: HCursor): Decoder.Result[Metadata] =
      for {
        from   <- c.downField("from").as[String]
        digest <- c.downField("digest").as[String]
      } yield Metadata(from, digest)
  }

  given encodeRegistryRequest: Encoder[RegistryRequest] =
    new Encoder[RegistryRequest]:
      final def apply(a: RegistryRequest): Json =
        Json.obj(
          ("didDocument", encodeDIDDoc.apply(a.didDocument)),
          (
            "options",
            Json.obj(a.options.map { case (k: String, v: String) =>
              (k, Json.fromString(v))
            }.toSeq: _*)
          ),
          (
            "secret",
            Json.obj(a.secret.map { case (k: String, v: String) =>
              (k, Json.fromString(v))
            }.toSeq: _*)
          )
        )
