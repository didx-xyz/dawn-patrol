package xyz.didx
import cats.effect.*
import cats.effect.IOApp
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.IO
import cats.effect.std.Dispatcher
import fs2.Stream

import scala.concurrent.duration.*

import xyz.didx.ConversationPollingHandler
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.SttpBackend
import xyz.didx.ai.handler.Opportunities
object DawnPatrol extends IOApp.Simple:
  // override protected def blockedThreadDetectionEnabled = true
  given logger: Logger[IO]            = Slf4jLogger.getLogger[IO]
  val pollingInterval: FiniteDuration = 10.seconds
  val pollingHandler                  = new ConversationPollingHandler(using logger)
  val run                             = Dispatcher[IO].use { dispatcher =>
    val pollingStream = for {
      backend: SttpBackend[cats.effect.IO, Any] <- Stream.resource(
                                                     AsyncHttpClientCatsBackend.resource[IO]()
                                                   )
      _                                         <- Stream.fixedRate[IO](10.seconds) // Poll every 10 seconds
      data                                      <- Stream.eval(pollingHandler.converse(backend))
    } yield data

    val pollingWithLogging = pollingStream.compile.drain
    for {
      _     <- Opportunities.fetchAndStoreOpportunities()
      fiber <- pollingWithLogging.start
      _     <- fiber.join
    } yield ()
  }
