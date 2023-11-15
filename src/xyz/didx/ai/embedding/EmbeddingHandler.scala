package xyz.didx.ai.embedding

import scala.collection.JavaConverters._
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.{EmbeddingMatch, EmbeddingStore}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.data.document.Metadata
import xyz.didx.ai.model.Opportunity
import java.time.Duration
import dev.langchain4j.model.output.Response

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

  def getEmbedding(input: String): Response[Embedding] =
    embeddingModel.embed(input.take(256))

  def getEmbedding(input: TextSegment): Response[Embedding] =
    embeddingModel.embed(input)

  def embedAll(inputList: java.util.List[TextSegment]): Response[java.util.List[Embedding]] =
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
    val embedding = getEmbedding(input).content()
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
          .add("opportunityURL", opportunity.opportunityURL)

        val opportunityEmbeddingInput: String =
          removeSpecialCharacters(s"""${opportunity.title} ${opportunity.description}""")
            .take(256)

        scribe.debug(s"Sample embedding input: $opportunityEmbeddingInput")

        TextSegment.from(opportunityEmbeddingInput, metadata)
      }
      .asJava

    val embeddings = embedAll(textSegments)
    storeAllEmbeddings(embeddings.content(), textSegments)
  }

  def findMostRelevantFromQuery(
    queryText: String,
    maxResults: Int = 1,
    minScore: Double = 0.7
  ): Option[EmbeddingMatch[TextSegment]] = {
    val queryEmbedding: Response[Embedding]                 = embeddingModel.embed(queryText.take(256))
    val relevant: List[EmbeddingMatch[TextSegment]]         =
      embeddingStore.findRelevant(queryEmbedding.content(), maxResults, minScore).asScala.toList
    val embeddingMatch: Option[EmbeddingMatch[TextSegment]] = relevant.headOption
    embeddingMatch match
      case None        =>
        scribe.info("No matching opportunity found for query")
        None
      case Some(value) =>
        scribe.info(s"Got embedding match with score: ${value.score()}")
        scribe.info(s"Got embedding match with text: ${value.embedded()}")
        Some(value)
  }

  def cosineSimilarity(f1: Array[Float], f2: Array[Float]): Double = {
    require(f1.length == f2.length, "Vectors must have the same length")

    val num  = (f1, f2).zipped.map(_ * _).sum
    val den1 = math.sqrt(f1.map(math.pow(_, 2)).sum)
    val den2 = math.sqrt(f2.map(math.pow(_, 2)).sum)

    num / (den1 * den2)
  }

}
