package xyz.didx.ai.handler

import scala.collection.mutable
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.store.ConversationId
import xyz.didx.ai.model.OnboardingResult
import xyz.didx.ai.model.AgentScript

object OnboardingHandler {
  // Define a map from conversationId to JvmPromptBuilder
  private val builders: mutable.Map[String, PromptBuilder] = mutable.Map()

  def getResponse(input: String, conversationId: String, telNo: Option[String] = None): OnboardingResult = {
    scribe.info(
      s"Get OnboardingHandler response for message: $input, for conversationId: $conversationId"
    )
    // Get the builder for this conversationId, or create a new one if it doesn't exist
    val builder = builders.getOrElseUpdate(
      conversationId,
      AgentScript.createYomaOnboardingBuilder(telNo)
    )

    // Add the user message to the builder
    builder.addUserMessage(input)

    // Create a new conversation with the specific conversationId
    conversation(
      {
        val result = prompt[OnboardingResult](builder.build())
        scribe.info(f"We have the following onboarding result: $result")
        result
      },
      conversationId = Some(ConversationId(conversationId))
    )
  }
}
