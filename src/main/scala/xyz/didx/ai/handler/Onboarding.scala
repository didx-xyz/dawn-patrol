package xyz.didx.ai.handler

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.store.ConversationId
import xyz.didx.ai.model.AgentScript
import xyz.didx.ai.model.OnboardingResult

import scala.collection.mutable
import scala.util.Random

object OnboardingHandler {
  // Define a map from conversationId to JvmPromptBuilder
  private val builders: mutable.Map[String, PromptBuilder] = mutable.Map()
  private val conversationIds: mutable.Map[String, String] = mutable.Map()

  def getResponse(
    input: String,
    conversationId: String,
    telNo: Option[String] = None,
    cleanSlate: Boolean = false
  ): OnboardingResult = {
    scribe.info(
      s"Get OnboardingHandler response for message: $input, for conversationId: $conversationId"
    )

    val defaultPromptBuilder   = AgentScript.createYomaOnboardingBuilder(telNo)
    val randomString: String   = Random.alphanumeric.take(10).mkString
    val builder: PromptBuilder = cleanSlate match
      case true  =>
        // We want to restart the onboarding on a clean slate, so update builders and use default
        conversationIds.update(conversationId, randomString) // reset conversationId
        builders.update(conversationId, defaultPromptBuilder)
        defaultPromptBuilder
      case false =>
        // Get the builder for this conversationId, or create a new one if it doesn't exist
        conversationIds.getOrElseUpdate(conversationId, randomString) // update conversationId if not exists
        builders.getOrElseUpdate(conversationId, defaultPromptBuilder)

    // Add the user message to the builder
    builder.addUserMessage(input)

    // Create a new conversation with the specific conversationId
    val xefConversationId = Some(ConversationId(conversationIds.getOrElse(conversationId, "default")))
    conversation(
      {
        val result = prompt[OnboardingResult](builder.build())
        scribe.info(f"We have the following onboarding result: $result")
        result
      },
      conversationId = xefConversationId
    )
  }
}
