package xyz.didx.ai.handler

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.scala.conversation._
import com.xebia.functional.xef.store.ConversationId
import xyz.didx.ai.model.AgentScript
import xyz.didx.ai.model.ConfirmedOnboardingResult
import xyz.didx.ai.model.OnboardingResult

import scala.collection.mutable

object ConfirmOnboardingHandler {
  def getConfirmation(
    input: String,
    conversationId: String
  ): ConfirmedOnboardingResult = {
    scribe.info(
      s"Get ConfirmOnboardingResult response for message: $input, for conversationId: $conversationId"
    )

    // Get the prompt builder for this script
    val builder = AgentScript.createConfirmationBuilder()

    // Add the user message to the builder
    builder.addUserMessage(input)

    // Create a new conversation with the specific conversationId
    conversation {
      val result = prompt[ConfirmedOnboardingResult](builder.build())
      scribe.info(f"We have the following confirmation result: $result")
      result
    }
  }

  def getConfirmationMessage(result: OnboardingResult): String =
    "Thank you. Please confirm if the following recorded data is correct:\n\n" +
      s"Name: ${result.fullName.getOrElse("None")}\n" +
      s"Email: ${result.email.getOrElse("None")}\n" +
      s"Cellphone: ${result.cellphone.getOrElse("None")}"

  def getReconfirmationMessage(result: OnboardingResult): String =
    "Apologies, we couldn't infer if you confirmed or not. " +
      "Please indicate \"yes\" or \"no\" if the following is correct:\n\n" +
      s"Name: ${result.fullName.getOrElse("None")}\n" +
      s"Email: ${result.email.getOrElse("None")}\n" +
      s"Cellphone: ${result.cellphone.getOrElse("None")}"

}
