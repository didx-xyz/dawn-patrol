package xyz.didx.ai

import scala.collection.mutable
import xyz.didx.ai.model.ChatState
import xyz.didx.ai.handler.OnboardingHandler
import xyz.didx.ai.handler.ConfirmOnboardingHandler
import dev.langchain4j.data.segment.TextSegment
import xyz.didx.ai.embedding.EmbeddingHandler
import dev.langchain4j.store.embedding.EmbeddingMatch
import xyz.didx.ai.model.OnboardingResult
import xyz.didx.ai.model.ConfirmedOnboardingResult

object AiHandler {
  private val onboardingResults: mutable.Map[String, OnboardingResult] = mutable.Map()

  def getAiResponse(
    input: String,
    conversationId: String,
    state: ChatState = ChatState.Onboarding,
    telNo: Option[String] = None
  ): (String, ChatState) = {
    scribe.info(
      s"Get AI response for message: $input, for conversationId: $conversationId, in state: $state"
    )

    state match
      case ChatState.Onboarding =>
        val result: OnboardingResult   = OnboardingHandler.getResponse(input, conversationId, telNo)
        val (messageToUser, nextState) = result match {
          case OnboardingResult(_, Some(fullName), Some(email), Some(cellphone)) =>
            // Results are records. Store in onboardingResults Map, and move to confirmation state
            onboardingResults.update(conversationId, result)

            val response = "Thank you. Please confirm if the following recorded data is correct:\n\n" +
              s"Name: ${result.fullName.getOrElse("None")}\n" +
              s"Email: ${result.email.getOrElse("None")}\n" +
              s"Cellphone: ${result.cellphone.getOrElse("None")}"

            (response, ChatState.ConfirmOnboardingResult)

          case OnboardingResult(_, _, _, _) => // In case data is not yet fully captured, remain in same state
            (result.nextMessageToUser, state)
        }
        (messageToUser, nextState)

      case ChatState.ConfirmOnboardingResult =>
        val onboardingResultOpt = onboardingResults.get(conversationId)

        onboardingResultOpt match
          case None                   => ("Something went wrong retrieving recorded results. Let's try again!", ChatState.Onboarding)
          case Some(onboardingResult) =>
            val confirmationResult = ConfirmOnboardingHandler.getConfirmation(input, onboardingResult, conversationId)
            confirmationResult.confirmed match
              case None        => (confirmationResult.nextMessageToUser, state)
              case Some(true)  => (
                  "Great, you are now ready to query the available Yoma opportunities! What would you like to do?",
                  ChatState.QueryingOpportunities
                )
              case Some(false) =>
                (
                  "Let's amend your details. What data needs to be corrected?",
                  ChatState.Onboarding
                ) // todo: add amend data state

      case ChatState.QueryingOpportunities =>
        val embeddingMatch: EmbeddingMatch[TextSegment] =
          EmbeddingHandler.findMostRelevantFromQuery(input)

        val logResult =
          s"From user request: $input\n" +
            "Got embedding match with: " +
            s"score = ${embeddingMatch.score()}, " +
            s"embedded = ${embeddingMatch.embedded()}, " +
            s"embeddingId = ${embeddingMatch.embeddingId()}"

        scribe.info(logResult)

        val response = embeddingMatch.embedded().toString
        (response, state)
      case ChatState.Done                  => ???
  }
}
