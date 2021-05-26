package emkn.w1

import cats.Monad
import cats.effect.Sync


trait Controller[F[_], Req, Resp] {
  def apply(request: Req): F[Resp]
}

sealed trait Request

object Request {

  case class Add(item: Data) extends Request

  case class Get(key: String) extends Request

  case object PrintAll extends Request

  case object GroupByValue extends Request

}

trait Printer[F[_]] {
  def println(v: Any): F[Unit]
}

class ConsolePrinter[F[_]: Sync] extends Printer[F] {
  override def println(v: Any): F[Unit] = Sync[F].blocking(Console.println(v))
}

final class DataController[F[_]: Monad](storage: Repository[F, Data, String])(implicit printer: Printer[F]) extends Controller[F, Request, Unit] {
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  import cats.syntax.traverse._

  override def apply(request: Request): F[Unit] = request match {
    case Request.PrintAll => storage.all().flatMap(_.traverse(printer.println).void)
    case Request.GroupByValue =>
      storage.all().flatMap(
        _.groupBy(_.value).toList.traverse(p =>
          printer.println(p._2)
        ).void
      )
    case Request.Add(item) => storage.put(item.key, item)
    case Request.Get(key) =>
      storage.get(key).flatMap {
        case Some(value) => printer.println(value)
        case None        => printer.println("Not Found")
      }
  }
}