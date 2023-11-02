package xyz.didx.ai

import xyz.didx.ai.model.ChatState
import xyz.didx.ai.handler.OnboardingHandler
import dev.langchain4j.data.segment.TextSegment
import xyz.didx.ai.embedding.EmbeddingHandler
import dev.langchain4j.store.embedding.EmbeddingMatch
import xyz.didx.ai.model.OnboardingResult

object AiHandler {

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
      case ChatState.Onboarding            =>
        val result: OnboardingResult   = OnboardingHandler.getResponse(input, conversationId, telNo)
        val (messageToUser, nextState) = result.confirmed match {
          case true  =>
            (
              "Great, you are now ready to query the available Yoma opportunities! What would you like to do?",
              ChatState.QueryingOpportunities
            )
          case false => (result.nextMessageToUser, state)
        }
        (messageToUser, nextState)
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
