package demo

import cats.arrow.FunctionK
import cats.data.EitherT
import cats.{Eval, Id, Monad}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

object Main {

  trait Storage[F[_], T] {
    def dump(item: T): F[Unit]
    def load():  F[Either[String, T]]
  }

  trait Serializer[T] {
    def serialize(item: T): String
  }

  trait Deserializer[T] {
    def deserialize(data: String): Either[String, T]
  }

  case class Foo(a: String, b: String)

  implicit val fooCodec = new Serializer[Foo] with Deserializer[Foo] {
    override def serialize(item: Foo): String = s"${item.a}:${item.b}"

    override def deserialize(data: String): Either[String, Foo] = data.split(':') match {
      case Array(a, b) => Right(Foo(a, b))
      case _          => Left(s"Failed to parse: ${data}")
    }
  }

  class ConsoleStorage[F[_]: Monad, T: Serializer: Deserializer] extends Storage[F, T] {
    override def dump(item: T): F[Unit] = Monad[F].pure(println(implicitly[Serializer[T]].serialize(item)))

    override def load(): F[Either[String, T]] = Monad[F].pure(implicitly[Deserializer[T]].deserialize(scala.io.StdIn.readLine()))
  }

  class ConsoleStorageV2[T: Serializer: Deserializer](implicit ec: ExecutionContext) extends Storage[Future, T] {
    override def dump(item: T): Future[Unit] =
      Future {
        println(s"Item=${implicitly[Serializer[T]].serialize(item)}")
      }

    override def load(): Future[Either[String, T]] = Future(implicitly[Deserializer[T]].deserialize(scala.io.StdIn.readLine()))
  }


  class BusinessLogic(storage: Storage[Future, Foo]) {
    def stepOne(): Future[Unit] = storage.dump(Foo("1", "1"))
    def stepTwo(): Future[Unit] = storage.dump(Foo("2", "2"))
    def stepThree(): Future[Unit] = storage.dump(Foo("3", "3"))
  }


  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import cats.syntax._
    import cats.implicits._

    // Future[[Either, Unit]] =>

    new FunctionK[Future, Option] {
      override def apply[A](fa: Future[A]): Option[A] =
       Try(Await.result(fa, Duration.Inf)).toOption
    }

    val console = new ConsoleStorage[Future, Foo]
    val consoleV2 = new ConsoleStorageV2[Foo]

    val f1 = for {
      _ <- console.dump(Foo("John", "Done"))
      item <- console.load()
    } yield println(item)

    val logic = new BusinessLogic(consoleV2)

    val f = for {
     _ <- logic.stepOne()
     _ <- logic.stepTwo()
     _ <- logic.stepThree()
    } yield ()

    Await.ready(f, Duration.Inf)
  }
}
