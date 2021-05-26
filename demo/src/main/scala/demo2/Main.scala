package demo2

import cats.data.{Reader, ReaderT}
import cats.{Applicative, Defer}
import cats.effect.{ExitCode, IO, IOApp, Resource, Sync, Temporal}

import scala.concurrent.duration._


object Main extends IOApp {
  import cats.syntax.applicative._
  import cats.syntax.functor._
  def factorial[F[_]: Applicative: Defer](n:BigDecimal): F[BigDecimal] = {
    if(n == 1) BigDecimal(1).pure
    else Defer[F].defer(
      factorial(n - 1).map(_ * n)
    )
  }

  def stepResource(name: String): Resource[IO, Unit] =
    Resource.make(IO(println(s">> ${name}")))(_ =>
      IO(println(s"<< $name"))
    )
  val logResource: Resource[IO, Unit] = Resource.make(IO(println("Start")))(_ => IO(println("Stop")))
  val elapsedResource: Resource[IO, Long] = Resource.make(IO(System.currentTimeMillis()))(startTs => IO {
    val elapsed = System.currentTimeMillis() - startTs
    println(s"Elapsed: ${elapsed}ms")
  })

  def dummyResource[A](create: IO[A])(release: A => IO[Unit]): IO[(A, IO[Unit])] = {
    create.map { instance =>
      (instance, release(instance))
    }
  }

  val x: IO[Unit] = for {
    r1 <- dummyResource(IO(println("1.1")))(_ => IO(println("1.2")))
    r2 <- dummyResource(IO(println("2.1")))(_ => IO(println("2.2")))
    // use ....
    _ <- r2._2
    _ <- r1._2
  } yield ()

  def repeatWithFixDelay[A](delay: FiniteDuration, f: IO[A]): IO[Unit] =
    f.flatMap(_ => Temporal[IO].sleep(delay))
      .flatMap(_ => repeatWithFixDelay(delay, f))

  val sayHello = ReaderT[IO, String, Unit] { name =>
    IO(println(s"Hello, ${name}"))
  }
  def walk[F[_]: Sync] = Sync[F].delay(println("WALK"))
  val sayBy = ReaderT[IO, String, Unit] { name =>
    IO(println(s"By, ${name}"))
  }

  type SayT[A] = ReaderT[IO, String, A]

  override def run(args: List[String]): IO[ExitCode] = {
    val appResource: Resource[IO, Unit] = for {
    _ <- stepResource("Step 1")
    _ <- logResource
    _ <- stepResource("Step 2")
    _ <- elapsedResource
    _ <- stepResource("Step 3")
    } yield ()

    val res = appResource.use(_ =>
      factorial[IO](10000).map { f =>
        println(s"Factorial = $f")
      }.flatMap(_ =>
        repeatWithFixDelay(1.second, IO(println("Tick")))
      ).map(_ => ExitCode.Success)
    )
    res
  }
}
