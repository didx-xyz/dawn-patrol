package xyz.didx.ai

import cats.effect.IO
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingMatch
import xyz.didx.ai.embedding.EmbeddingHandler
import xyz.didx.ai.handler.ConfirmOnboardingHandler
import xyz.didx.ai.handler.OnboardingHandler
import xyz.didx.ai.model.ChatState
import xyz.didx.ai.model.OnboardingResult

import scala.collection.mutable
import scala.util.Try

object AiHandler {
  private val onboardingResults: mutable.Map[String, OnboardingResult] = mutable.Map()

  def getAiResponse(
    input: String,
    conversationId: String,
    state: ChatState = ChatState.Onboarding,
    telNo: Option[String] = None
  ): IO[Either[Error, (String, ChatState)]] = {
    scribe.info(
      s"Get AI response for message: $input, for conversationId: $conversationId, in state: $state"
    )
    println(
      s"Get AI response for message: $input, for conversationId: $conversationId, in state: $state"
    )

    state match
      case ChatState.Onboarding =>
        IO(Try {
          val result: OnboardingResult   = OnboardingHandler.getResponse(input, conversationId, telNo)
          scribe.info(
            s"Got response from OnboardingHandler::getResponse, for conversationId: $conversationId"
          )
          val (messageToUser, nextState) = result match {
            case OnboardingResult(_, Some(fullName), Some(email), Some(cellphone)) =>
              scribe.info(
                s"Recorded onboarding result for conversationId: $conversationId"
              )

              onboardingResults.update(conversationId, result) // Store in onboardingResults Map

              val response = ConfirmOnboardingHandler.getConfirmationMessage(result)

              (response, ChatState.ConfirmOnboardingResult) // move to confirmation state

            case OnboardingResult(_, _, _, _) =>
              scribe.info(
                s"Data is not yet fully captured, remain in same state for conversationId: $conversationId"
              )(result.nextMessageToUser, state)
          }
          (messageToUser, nextState)
        }.toEither.left.map(e => new Error(e.getMessage())))

      case ChatState.ConfirmOnboardingResult =>
        IO(Try {
          val onboardingResultOpt = onboardingResults.get(conversationId)
          onboardingResultOpt match
            case None                   => ("Something went wrong retrieving recorded results. Let's try again!", ChatState.Onboarding)
            case Some(onboardingResult) =>
              val confirmationResult = ConfirmOnboardingHandler.getConfirmation(input, conversationId)
              confirmationResult.userHasConfirmedOptionalBool match
                case None        => (
                    ConfirmOnboardingHandler.getReconfirmationMessage(onboardingResult),
                    ChatState.ConfirmOnboardingResult
                  )
                case Some(true)  => (
                    "Great, you are now ready to query the available Yoma opportunities! What would you like to do?",
                    ChatState.QueryingOpportunities
                  )
                case Some(false) =>
                  (
                    "Let's amend your details. What data needs to be corrected?",
                    ChatState.Onboarding
                  )
            // todo: add amend data state
        }.toEither.left.map(e => new Error(e.getMessage())))

      case ChatState.QueryingOpportunities =>
        IO(Try {
          input.toLowerCase.contains("onboard") match
            case true  => // provide a way to switch back to onboarding, out from querying opportunities
              (
                OnboardingHandler.getResponse(input, conversationId, telNo, cleanSlate = true).nextMessageToUser,
                ChatState.Onboarding
              )
            case false => // otherwise, query opportunities as usual:
              val embeddingMatchOpt: Option[EmbeddingMatch[TextSegment]] =
                EmbeddingHandler.findMostRelevantFromQuery(input)

              embeddingMatchOpt match
                case None                 => (
                    "Apologies, we couldn't find a relevant opportunity. Please try a different request!",
                    state
                  )
                case Some(embeddingMatch) =>
                  val logResult =
                    s"From user request: $input\n" +
                      "Got embedding match with: " +
                      s"score = ${embeddingMatch.score()}, " +
                      s"embedded = ${embeddingMatch.embedded()}, " +
                      s"embeddingId = ${embeddingMatch.embeddingId()}"

                  scribe.info(logResult)

                  val topMatch: TextSegment  = embeddingMatch.embedded()
                  val id: String             = topMatch.metadata("id")
                  val title: String          = topMatch.metadata("title")
                  val organisation: String   = topMatch.metadata("organisationName")
                  val opportunityUrl: String = topMatch.metadata("opportunityURL")

                  val backupUrl: String = s"https://app.yoma.world/opportunities/$id"

                  val url = opportunityUrl match
                    case null | "null" | "" => backupUrl // handle potential edge cases
                    case _                  => opportunityUrl

                  val response: String =
                    s"You might be interested in: $title, by $organisation. Here's a link to the opportunity page: $url"

                  (response, state)
        }.toEither.left.map(e => new Error(e.getMessage())))

      case ChatState.Done => IO(Right(("Thank you for using DawnPatrol! Goodbye!", state)))
  }
}
