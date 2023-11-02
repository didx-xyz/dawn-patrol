package xyz.didx.ai.model

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder

case class OnboardingResult(
  @Description("The next message that you want to send to the user") nextMessageToUser: String,
  @Description("The full name of the user") fullName: Option[String] = None,
  @Description("The email address of the user") email: Option[String] = None,
  @Description("The cellphone number of the user") cellphone: Option[String] = None,
  @Description("User has confirmed captured fields are correct") confirmed: Boolean = false
) derives SerialDescriptor,
      Decoder
