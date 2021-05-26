package demo3

import cats.effect._

import scala.concurrent.duration._


object Main extends IOApp {
  def repeatWithFixDelay[A](delay: FiniteDuration, f: IO[A]): IO[Unit] =
    f.flatMap(_ => Temporal[IO].sleep(delay))
      .flatMap(_ => repeatWithFixDelay(delay, f))

  val ping = IO(println("ping"))

  def daemonize[A](io: IO[A]): Resource[IO, IO[Unit]] = {
    Resource.make(io.void.start)(_.cancel).map(_.joinWith(IO.unit))
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val res = for {
      a <- daemonize(
         repeatWithFixDelay(1.second, ping)
       )
      b <- daemonize(
        repeatWithFixDelay(2.second, ping)
      )
    } yield (a, b)

    res.use { case (value, value1) =>
      IO.race(value, value1).map(_.fold(identity, identity))
    }.as(ExitCode.Success)
  }
}
