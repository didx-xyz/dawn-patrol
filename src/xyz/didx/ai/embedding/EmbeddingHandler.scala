package xyz.didx.ai.embedding

import scala.collection.JavaConverters._
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.inprocess.{InProcessEmbeddingModel, InProcessEmbeddingModelType}
import dev.langchain4j.store.embedding.{EmbeddingMatch, EmbeddingStore}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.data.document.Metadata
import xyz.didx.ai.model.Opportunity
import java.time.Duration

object EmbeddingHandler {
  private val embeddingModel: EmbeddingModel = HuggingFaceEmbeddingModel
    .builder()
    .accessToken(sys.env("HF_API_KEY"))
    .modelId("sentence-transformers/all-MiniLM-L6-v2")
    .waitForModel(true)
    .timeout(Duration.ofSeconds(60))
    .build()

  private val embeddingStore: EmbeddingStore[TextSegment] =
    new InMemoryEmbeddingStore[TextSegment]()

  def getEmbedding(input: String): Embedding =
    embeddingModel.embed(input.take(256))

  def getEmbedding(input: TextSegment): Embedding =
    embeddingModel.embed(input)

  def embedAll(inputList: java.util.List[TextSegment]): java.util.List[Embedding] =
    embeddingModel.embedAll(inputList)

  def storeEmbedding(embedding: Embedding): Unit = {
    embeddingStore.add(embedding)
  }

  def storeEmbedding(embedding: Embedding, embedded: String): Unit = {
    embeddingStore.add(embedding, TextSegment.from(embedded))
  }

  def storeEmbedding(embedding: Embedding, embedded: TextSegment): Unit = {
    embeddingStore.add(embedding, embedded)
  }

  def storeAllEmbeddings(embeddings: java.util.List[Embedding]): Unit = {
    embeddingStore.addAll(embeddings)
  }

  def storeAllEmbeddings(
    embeddings: java.util.List[Embedding],
    textSegment: java.util.List[TextSegment]
  ): Unit = {
    embeddingStore.addAll(embeddings, textSegment)
  }

  def getAndStoreEmbedding(input: String): Unit = {
    val embedding = getEmbedding(input)
    storeEmbedding(embedding, input)
  }

  def removeSpecialCharacters(input: String): String =
    input
      .replaceAll("[^\\x00-\\x7F]", "") // emojis
      .replaceAll("""[!?&'"]""", "")    // punctuation
      .replaceAll(""" - """, " ")       // punctuation
      .replaceAll("""\\s+""", " ") // extra whitespace

  def getAndStoreAll(opportunities: List[Opportunity]): Unit = {
    val textSegments: java.util.List[TextSegment] = opportunities
      .filter { opportunity =>
        opportunity.language.contains("EN")
      }
      .map { opportunity =>
        val metadata = Metadata()
          .add("id", opportunity.id)
          .add("title", opportunity.title)
          .add("organisationName", opportunity.organisationName)

        val opportunityEmbeddingInput: String =
          removeSpecialCharacters(s"""${opportunity.title} ${opportunity.description}""")
            .take(256)

        scribe.info(s"Sample input: $opportunityEmbeddingInput")

        TextSegment.from(opportunityEmbeddingInput, metadata)
      }
      .asJava

    val embeddings: java.util.List[Embedding] = embedAll(textSegments)
    storeAllEmbeddings(embeddings, textSegments)
  }

  def findMostRelevantFromQuery(
    queryText: String,
    maxResults: Int = 1,
    minScore: Double = 0.7
  ): EmbeddingMatch[TextSegment] = {
    val queryEmbedding: Embedding                   = embeddingModel.embed(queryText.take(256))
    val relevant: List[EmbeddingMatch[TextSegment]] =
      embeddingStore.findRelevant(queryEmbedding, maxResults, minScore).asScala.toList
    val embeddingMatch                              = relevant.headOption
    embeddingMatch match
      case None        =>
        EmbeddingMatch(0.0, "na", Embedding(Array.emptyFloatArray), TextSegment.from("na"))
      case Some(value) =>
        scribe.info(s"Got embedding match with score: ${value.score()}")
        scribe.info(s"Got embedding match with text: ${value.embedded()}")
        value
  }

  def cosineSimilarity(f1: Array[Float], f2: Array[Float]): Double = {
    require(f1.length == f2.length, "Vectors must have the same length")

    val num  = (f1, f2).zipped.map(_ * _).sum
    val den1 = math.sqrt(f1.map(math.pow(_, 2)).sum)
    val den2 = math.sqrt(f2.map(math.pow(_, 2)).sum)

    num / (den1 * den2)
  }

}
