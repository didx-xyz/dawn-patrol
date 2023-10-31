package xyz.didx.ai.model

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder

case class OnboardingResult(
  @Description("The next message that you want to send to the user") nextMessageToUser: String,
  @Description("The full name as obtained from the user") fullName: Option[String] = None,
  @Description("The email address as obtained from the user") email: Option[String] = None,
  @Description("The cellphone number as obtained from the user") cellphone: Option[String] = None
) derives SerialDescriptor,
      Decoder
