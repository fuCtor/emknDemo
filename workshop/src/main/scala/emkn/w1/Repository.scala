package emkn.w1

import cats.Functor
import cats.effect.kernel.Sync

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

trait Repository[F[_], T, ID] {
  def put(item: T): F[Unit]

  def get(id: ID): F[Option[T]]

  def all(): F[List[T]]
}

trait DataRepository[F[_]] extends Repository[F, Data, String]

class MemoryDataRepository[F[_]: Sync] extends DataRepository[F] {

  private[this] val storage = new ConcurrentHashMap[String, Data]()

  override def put(item: Data): F[Unit] = Sync[F].delay {
    storage.put(item.key, item)
  }

  override def get(key: String): F[Option[Data]] = Sync[F].delay {
    Option(storage.get(key))
  }

  override def all(): F[List[Data]] = Sync[F].delay {
    storage.values().asScala.toList
  }
}

class PersistentDataRepository[F[_]: Functor](storage: Storage[F, Data]) extends DataRepository[F] {
  import cats.syntax.functor._
  override def put(item: Data): F[Unit] = storage.write(item)

  override def get(key: String): F[Option[Data]] = storage.readAll.map(_.find(_.key == key))

  override def all(): F[List[Data]] = storage.readAll
}