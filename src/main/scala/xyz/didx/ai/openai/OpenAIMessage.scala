package xyz.didx.openai

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
