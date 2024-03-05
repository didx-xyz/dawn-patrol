package xyz.didx.messages

import cats.Applicative.ops.toAllApplicativeOps
import cats._
import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Encoder
import io.circe.Json
import io.circe._
import io.circe.syntax._
import xyz.didx.messages.Member
import xyz.didx.messages.SignalMessage
import xyz.didx.messages.SignalSendMessage

object SignalMessageCodec:
  given memberDecoder: Decoder[Member] = new Decoder[Member]:
    def apply(c: HCursor): Result[Member] =
      for
        name   <- c.downField("name").as[String]
        number <- c.downField("number").as[String]
      yield Member(name, number)

  given signalSendMessage: Encoder[SignalSendMessage] =
    new Encoder[SignalSendMessage]:
      def apply(a: SignalSendMessage): Json = Json.obj(
        (
          "base64_attachments",
          Json.arr(a.attachments.map(Json.fromString): _*)
        ),
        ("message", Json.fromString(a.message)),
        ("number", Json.fromString(a.number)),
        ("recipients", Json.arr(a.recipients.map(Json.fromString): _*))
      )

  given signalMessageDecoder: Decoder[SignalMessage] =
    new Decoder[SignalMessage]:
      def apply(c: HCursor): Result[SignalMessage] =
        for
          envelope <- c.downField("envelope").as[SignalEnvelope]
          account  <- c.downField("account").as[String]
        yield SignalMessage(envelope, account)

  given signalEnvelopeDecoder: Decoder[SignalEnvelope] =
    new Decoder[SignalEnvelope]:
      def apply(c: HCursor): Result[SignalEnvelope] =
        for {
          source         <- c.downField("source").as[String]
          sourceNumber   <- c.downField("sourceNumber").as[String]
          sourceUuid     <- c.downField("sourceUuid").as[String]
          sourceName     <- c.downField("sourceName").as[String]
          sourceDevice   <- c.downField("sourceDevice").as[Int]
          timestamp      <- c.downField("timestamp").as[Long]
          dataMessage     =
            c.downField("dataMessage").focus match
              case Some(json) =>
                json.as[SignalDataMessage] match
                  case Right(value) => Some(value)
                  case Left(error)  => None
              case None       => Option.empty[SignalDataMessage]
          receiptMessage <- c
                              .downField("receiptMessage")
                              .as[Option[SignalReceiptMessage]]
          syncMessage    <- c
                              .downField("syncMessage")
                              .as[Option[SignalSyncMessage]]
          sentMessage    <- c
                              .downField("sentMessage")
                              .as[Option[SignalSentMessage]]
        } yield SignalEnvelope(
          source,
          sourceNumber,
          sourceUuid,
          sourceName,
          sourceDevice,
          timestamp,
          dataMessage,
          receiptMessage,
          syncMessage,
          sentMessage
        )

  given signalDataMessageDecoder: Decoder[SignalDataMessage] =
    new Decoder[SignalDataMessage]:
      def apply(c: HCursor): Result[SignalDataMessage] =
        for {
          timestamp        <- c.downField("timestamp").as[Long]
          message          <- c.downField("message").as[String]
          expiresInSeconds <- c.downField("expiresInSeconds").as[Int]
          viewOnce         <- c.downField("viewOnce").as[Boolean]
          groupInfo        <- c.downField("groupInfo").as[Option[SignalGroupInfo]]
        } yield SignalDataMessage(
          timestamp,
          message,
          expiresInSeconds,
          viewOnce,
          groupInfo
        )

  given signalGroupInfoDecoder: Decoder[SignalGroupInfo] =
    new Decoder[SignalGroupInfo]:
      def apply(c: HCursor): Result[SignalGroupInfo] =
        for
          groupId <- c.downField("groupId").as[String]
          `type`  <- c.downField("type").as[String]
        yield SignalGroupInfo(groupId, `type`)

  given signalReceiptMessageDecoder: Decoder[SignalReceiptMessage] =
    new Decoder[SignalReceiptMessage]:
      def apply(c: HCursor): Result[SignalReceiptMessage] =
        for
          when       <- c.downField("when").as[Long]
          isDelivery <- c.downField("isDelivery").as[Boolean]
          isRead     <- c.downField("isRead").as[Boolean]
          isViewed   <- c.downField("isViewed").as[Boolean]
          timestamps <- c.downField("timestamps").as[List[Long]]
        yield SignalReceiptMessage(
          when,
          isDelivery,
          isRead,
          isViewed,
          timestamps
        )

  given signalSyncMessageDecoder: Decoder[SignalSyncMessage] =
    new Decoder[SignalSyncMessage]:
      def apply(c: HCursor): Result[SignalSyncMessage] =
        for {
          sentMessage  <- c
                            .downField("sentMessage")
                            .as[Option[SignalSentMessage]]
          readMessages <- c
                            .downField("readMessages")
                            .as[Option[List[SignalReadMessage]]]
        } yield SignalSyncMessage(sentMessage, readMessages)

  given signalSentMessageDecoder: Decoder[SignalSentMessage] =
    new Decoder[SignalSentMessage]:
      def apply(c: HCursor): Result[SignalSentMessage] =
        for
          destination       <- c.downField("destination").as[String]
          destinationNumber <- c.downField("destinationNumber").as[String]
          destinationUuid   <- c.downField("destinationUuid").as[String]
          timestamp         <- c.downField("timestamp").as[Long]
          message           <- c.downField("message").as[String]
          expiresInSeconds  <- c.downField("expiresInSeconds").as[Int]
          viewOnce          <- c.downField("viewOnce").as[Boolean]
          groupInfo         <- c.downField("groupInfo").as[Option[SignalGroupInfo]]
        yield SignalSentMessage(
          destination,
          destinationNumber,
          destinationUuid,
          timestamp,
          message,
          expiresInSeconds,
          viewOnce,
          groupInfo
        )

  given signalReadMessageDecoder: Decoder[SignalReadMessage] =
    new Decoder[SignalReadMessage]:
      def apply(c: HCursor): Result[SignalReadMessage] =
        for
          sender       <- c.downField("sender").as[String]
          senderNumber <- c.downField("senderNumber").as[String]
          senderUuid   <- c.downField("senderUuid").as[String]
          timestamp    <- c.downField("timestamp").as[Long]
        yield SignalReadMessage(sender, senderNumber, senderUuid, timestamp)

  given signalSimpleMessageDecoder: Decoder[SignalSimpleMessage] =
    new Decoder[SignalSimpleMessage]:
      def apply(c: HCursor): Result[SignalSimpleMessage] =
        for
          message  <- c.downField("message").as[String]
          number   <- c.downField("number").as[String]
          text     <- c.downField("text").as[String]
          keywords <- c.downField("keywords").as[List[String]]
        yield SignalSimpleMessage(message, number, text, keywords)
