package xyz.didx.openai
/* {
  "id": "cmpl-6nognZq5jEzrrxtd7hkQ06vQcNOAu",
  "object": "text_completion",
  "created": 1677330941,
  "model": "text-davinci-003",
  "choices": [
    {
      "text": "\nIt's important to remember to renew your drivers license before it expires. You can usually renew your license online or in person at your local DMV. Make sure to check the expiration date on your license and plan ahead so you don't miss the deadline.",
      "index": 0,
      "logprobs": null,
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 9,
    "completion_tokens": 51,
    "total_tokens": 60
  }
}
 */
final case class OpenAIRequest(
  model: String = "text-davinci-003",
  prompt: String,
  temperature: Double = 0.5,
  maxTokens: Int = 60,
  topLogProbs: Double = 1.0,
  frequencyPenalty: Double = 0.8,
  presencePenalty: Double = 0.0
)

final case class OpenAIResponse(
  id: String,
  `object`: String,
  created: Int,
  model: String,
  choices: List[OpenAIResponseChoice],
  usage: OpenAIResponseUsage
)

final case class OpenAIResponseChoice(
  text: String,
  index: Int,
  logProbs: Option[OpenAIResponseChoiceLogProbs],
  finish_reason: Option[String]
)

final case class OpenAIResponseChoiceLogProbs(
  tokenLogProbs: Option[Map[String, List[Double]]],
  topLogProbs: Option[Map[String, List[Double]]],
  textOffset: Option[Map[String, List[Double]]]
)

final case class OpenAIResponseUsage(
  promptTokens: Int,
  completionTokens: Int,
  totalTokens: Int
)
