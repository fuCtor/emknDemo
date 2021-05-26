package emkn.w1

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  import Request._

  val repo = new MemoryDataRepository[IO]
  implicit val printer = new ConsolePrinter[IO]
  val controller = new DataController(repo)

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- controller(Add(Data("a", "b")))
      _ <- controller(Add(Data("d", "b")))
      _ <- controller(Add(Data("e", "b")))
      _ <- controller(Add(Data("b", "c")))
      _ <- controller(PrintAll)
      _ <- controller(GroupByValue)
      _ <- controller(Get("a"))
      _ <- controller(Get("c"))
    } yield ExitCode.Success
}
