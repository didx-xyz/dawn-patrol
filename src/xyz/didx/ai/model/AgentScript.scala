package xyz.didx.ai.model

import com.xebia.functional.xef.prompt.JvmPromptBuilder
import com.xebia.functional.xef.prompt.PromptBuilder

object AgentScript {
  def createYomaOnboardingBuilder(telNo: Option[String] = None): PromptBuilder = {
    val baseMessage =
      "You are Yoma, an onboarding assistant! in charge of obtaining the following information from the user: "

    val conditionalMessage = telNo match {
      case Some(number) =>
        "Name; Email. " +
          s"We already know the cellphone number of the user: it is $number. Don't ask them for their cellphone number. "
      case None         =>
        "Name; Email; Cellphone. "
    }

    val endMessage = "When receiving your first message from a user, begin asking for the info you still need. " +
      "They can give one attribute at a time, or all at once. " +
      "If a user has already given their name, email or cellphone in the chat, or if you already know it, then you shouldn't ask them again." +
      "Be friendly."

    val fullMessage = s"$baseMessage $conditionalMessage $endMessage"
    new JvmPromptBuilder().addSystemMessage(fullMessage)
  }

  def createConfirmationBuilder(): PromptBuilder = {
    val baseMessage =
      "Your job is to receive confirmation from the user - whether the data we've recorded is correct or not. " +
        "They might respond with: yes, indeed, correct, of course, :+1+, a thumbs up emoji, slang like shap, or other general confirmation - all of this would confirm their data (confirmed = True). " +
        "If they respond with: no, not correct, incorrect, of course not, :-1+, a thumbs down emoji, slang like wtf, or other general rejection - all of this would confirm their data is incorrect (confirmed = False). " +
        "If they give an unclear, blank, neutral or irrelevant response, we'll consider this to be confirmed = None, in which case we will prompt them again. " +
        "Please process their response to establish if confirmed = True, False, or None. "

    new JvmPromptBuilder().addSystemMessage(baseMessage)
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
