package xyz.didx.signal.messages

import cats.effect.IO
import io.circe.parser.*

case class SignalSendMessage(
  attachments: List[String],
  message: String,
  number: String,
  recipients: List[String]
):
  override def toString(): String =
    s"""SignalSendMessage(
      message: $message,
      number: $number,
      recipients: $recipients,
      attachments: ${attachments.mkString(",")}
    )"""

case class SignalMessage(val envelope: SignalEnvelope, account: String):
  override def toString(): String =
    s"""SignalMessage(
      envelope: $envelope,
      account: $account
    )"""

case class SignalSimpleMessage(
  phone: String,
  name: String,
  text: String,
  keywords: List[String] = List[String]()
):
  override def toString(): String =
    s"""SignalSimpleMessage(
      phone: $phone,
      name: $name,
      text: $text,
      keywords: ${keywords.mkString(",")}
    )"""
