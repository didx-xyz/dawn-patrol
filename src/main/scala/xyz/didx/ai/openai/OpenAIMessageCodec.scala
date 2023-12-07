package xyz.didx.openai

import cats.*
import cats.syntax.all.*
import io.circe.Decoder.Result
import io.circe.*
import io.circe.syntax.*

object OpenAIMessageCodec {
  given openAIResponseDecoder: Decoder[OpenAIResponse] =
    new Decoder[OpenAIResponse] {
      def apply(c: HCursor): Result[OpenAIResponse] =
        for
          id       <- c.downField("id").as[String]
          `object` <- c.downField("object").as[String]
          created  <- c.downField("created").as[Int]
          model    <- c.downField("model").as[String]
          choices  <- c.downField("choices").as[List[OpenAIResponseChoice]]
          usage    <- c.downField("usage").as[OpenAIResponseUsage]
        yield OpenAIResponse(id, `object`, created, model, choices, usage)
    }

  given openAIChoiceDecoder: Decoder[OpenAIResponseChoice] =
    new Decoder[OpenAIResponseChoice] {
      def apply(c: HCursor): Result[OpenAIResponseChoice] =
        for
          text         <- c.downField("text").as[String]
          index        <- c.downField("index").as[Int]
          logprobs     <- c
                            .downField("logprobs")
                            .as[Option[OpenAIResponseChoiceLogProbs]]
          finishReason <- c.downField("finish_reason").as[Option[String]]
        yield OpenAIResponseChoice(text, index, logprobs, finishReason)
    }

  given openAILogProbsDecoder: Decoder[OpenAIResponseChoiceLogProbs] =
    new Decoder[OpenAIResponseChoiceLogProbs] {
      def apply(c: HCursor): Result[OpenAIResponseChoiceLogProbs] =
        for
          tokenLogProbs <- c
                             .downField("token_logprobs")
                             .as[Option[Map[String, List[Double]]]]
          textLogProbs  <- c
                             .downField("text_logprobs")
                             .as[Option[Map[String, List[Double]]]]
          textOffset    <- c
                             .downField("text_offset")
                             .as[Option[Map[String, List[Double]]]]
        yield OpenAIResponseChoiceLogProbs(
          tokenLogProbs,
          textLogProbs,
          textOffset
        )
    }

  given openAIUsageDecoder: Decoder[OpenAIResponseUsage] =
    new Decoder[OpenAIResponseUsage] {
      def apply(c: HCursor): Result[OpenAIResponseUsage] =
        for
          promptTokens     <- c.downField("prompt_tokens").as[Int]
          completionTokens <- c.downField("completion_tokens").as[Int]
          totalTokens      <- c.downField("total_tokens").as[Int]
        yield OpenAIResponseUsage(promptTokens, completionTokens, totalTokens)
    }

  given openAIRequestEncoder: Encoder[OpenAIRequest] =
    new Encoder[OpenAIRequest] {
      def apply(a: OpenAIRequest): Json = Json.obj(
        ("model", Json.fromString(a.model)),
        ("prompt", Json.fromString(a.prompt)),
        ("temperature", Json.fromDoubleOrNull(a.temperature)),
        ("max_tokens", Json.fromInt(a.maxTokens)),
        ("top_p", Json.fromDoubleOrNull(a.topLogProbs)),
        ("frequency_penalty", Json.fromDoubleOrNull(a.frequencyPenalty)),
        ("presence_penalty", Json.fromDoubleOrNull(a.presencePenalty))
      )
    }

}
