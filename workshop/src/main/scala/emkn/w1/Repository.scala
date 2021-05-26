package emkn.w1

import cats.Functor
import cats.effect.kernel.Sync

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

trait Repository[F[_], T, Id] {
  def put(id:Id, item: T): F[Unit]

  def get(id: Id): F[Option[T]]

  def all(): F[List[T]]
}

class MemoryRepository[F[_]: Sync, T, Id] extends Repository[F, T, Id] {

  private[this] val storage = new ConcurrentHashMap[Id, T]()

  override def put(id:Id, item: T): F[Unit] = Sync[F].delay {
    storage.put(id, item)
  }

  override def get(key: Id): F[Option[T]] = Sync[F].delay {
    Option(storage.get(key))
  }

  override def all(): F[List[T]] = Sync[F].delay {
    storage.values().asScala.toList
  }
}

class PersistentRepository[F[_]: Functor, T, Id](storage: Storage[F, T], getId: T => Id) extends Repository[F, T, Id] {
  import cats.syntax.functor._
  override def put(id:Id, item: T): F[Unit] = storage.write(item)

  override def get(id: Id): F[Option[T]] = storage.readAll.map(_.find(item => getId(item) == id))

  override def all(): F[List[T]] = storage.readAll
}