package xyz.didx.ai.model

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder

case class OnboardingResult(
  @Description("The next message that you want to send to the user") nextMessageToUser: String,
  @Description("The full name of the user") fullName: Option[String] = None,
  @Description("The email address of the user") email: Option[String] = None,
  @Description("The cellphone number of the user") cellphone: Option[String] = None
) derives SerialDescriptor,
      Decoder

case class ConfirmedOnboardingResult(
  @Description("Optional[Boolean] whether user has confirmed or not") userHasConfirmedOptionalBool: Option[
    Boolean
  ] = None
) derives SerialDescriptor,
      Decoder
