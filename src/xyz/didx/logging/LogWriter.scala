package xyz.didx.logging

import cats.data.EitherT
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.client3.ResponseException

object LogWriter:
  type ErrorOr[A] = EitherT[IO, Exception, A]
  // type LoggedEitherT[F[_], E, A] = WriterT[EitherT[F, E, *], Logger[F], A]

  def info[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    println(s"Main: $value")
    logger.info(s"$value")

  def err[T](value: T)(using logger: Logger[IO]): IO[Unit] =
    println(s"Main: $value")
    logger.error(s"$value")

  def logNonEmptyList[T](
    result: Either[ResponseException[String, io.circe.Error], List[T]]
  )(using logger: Logger[IO]): IO[Unit] =
    result match {
      case Right(list) if list.nonEmpty =>
        logger.info(s"Processing input: $list")
      case Left(e)                      =>
        logger.error(s"Error: $e")
      case _                            =>
        IO.unit // Do nothing
    }
