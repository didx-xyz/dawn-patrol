package xyz.didx
import cats.effect.*
object DawnPatrol extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    IO(println("Hello, world!")).as(ExitCode.Success)

}
