package emkn.w2

import cats.{Defer, Monad}

import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters._

class InMemoryRepository[F[_]: Monad: Defer, Id, Item] extends Repository[F, Id, Item] {
  import cats.syntax.applicative._
  import cats.syntax.functor._
  private[this] val storage = new ConcurrentHashMap[Id, Item]()

  override def put(id: Id, item: Item): F[Unit] = Defer[F].defer(
    Option(storage.put(id, item)).pure[F].void
  )

  override def get(id: Id): F[Option[Item]] = Defer[F].defer(
    Option(storage.get(id)).pure[F]
  )

  override def all(): F[List[Item]] = Defer[F].defer(
    storage.values().asScala.toList.pure[F]
  )
}
