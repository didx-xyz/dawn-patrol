package xyz.didx
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import cats.effect.std.Dispatcher
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import xyz.didx.ConversationPollingHandler
import xyz.didx.ai.handler.Opportunities
import xyz.didx.config.ConfigReaders.getConf

import scala.concurrent.duration.*

object DawnPatrol extends IOApp.Simple:
  // override protected def blockedThreadDetectionEnabled = true
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val appConf = getConf(using logger)

  val pollingInterval: FiniteDuration            = appConf.pollingInterval.seconds
  val pollingHandler: ConversationPollingHandler = new ConversationPollingHandler(using logger)

  val run = Dispatcher[IO].use { dispatcher =>
    val pollingStream = for {
      backend: SttpBackend[IO, Any] <- Stream.resource(
                                         AsyncHttpClientCatsBackend.resource[IO]()
                                       )
      _                             <- Stream.fixedRate[IO](pollingInterval)
      data                          <- Stream.eval(pollingHandler.converse(backend))
    } yield data

    val pollingWithLogging = pollingStream.compile.drain
    for {
      _       <- IO(println(s"Start polling at interval: $pollingInterval seconds"))
      _       <- Opportunities.fetchAndStoreOpportunities()
      attempt <- pollingWithLogging.attempt
      _       <- attempt.fold(
                   error => {
                     logger.error(s"Error: $error")
                     IO(println(s"Error: $error"))
                   },
                   _ => IO.unit
                 )
    } yield ()
  }
