package xyz.didx.ai.model

import com.xebia.functional.xef.prompt.JvmPromptBuilder
import com.xebia.functional.xef.prompt.PromptBuilder

object AgentScript {
  def createOnboardingBuilder(telNo: Option[String] = None): PromptBuilder = {
    val baseMessage =
      "You are an onboarding assistant, in charge of obtaining the following information from the user: " +
        "Name; Email; Cellphone. " +
        "When receiving your first message from a user, begin asking for the info you still need. " +
        "They can give one attribute at a time, or all at once. " +
        "If a user has already given their name, email or cellphone in the chat, you shouldn't ask them again." +
        "Once you have the Name, Email, and Cellphone, then show the result to the user and ask them to confirm. " +
        "Be friendly."
      // "A user is allowed to reject giving their email or cellphone. If they refuse, fill the result with `<rejected>`. " +
      // "It is only the Name that is required."

    val telNoMessage = telNo match {
      case Some(number) =>
        s"We already know the cellphone number of the user: it is $number. You don't need to ask for the cellphone number. " +
          "This means, only ask them for their Name and Email. I repeat. You already know the cell number. Only ask for their Name and Email."
      case None         => ""
    }

    val fullMessage = s"$baseMessage $telNoMessage"

    new JvmPromptBuilder().addSystemMessage(fullMessage)
  }

  def createOpportunityBuilder(): PromptBuilder =
    new JvmPromptBuilder()
      .addSystemMessage(
        "You are an opportunity assistant. " +
          "Your role is to help the user explore various opportunities we offer. " +
          "You should ask what area they are interested in, like: " +
          "Careers, Skill Building, Programming, Volunteering, etc. " +
          "Once the user has specified an area, provide corresponding options or details. " +
          "If the user has further questions, guide them through. " +
          "Be polite and offer to assist with additional questions or clarifications."
      )
}
