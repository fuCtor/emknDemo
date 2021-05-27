package emkn.w2

import cats.Monad

class PersistentRepository[F[_]: Monad, Id, Item](storage: Storage[F, Item], item2id: Item => Id) extends Repository[F, Id, Item] {
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  override def put(id: Id, item: Item): F[Unit] = for {
    items <- storage.load()
    filtered = items.filterNot(item2id(_) == id)
    _ <- storage.save(item :: filtered)
  } yield ()

  override def get(id: Id): F[Option[Item]] =
    storage.load().map(_.find( item => item2id(item) == id))

  override def all(): F[List[Item]] = storage.load()
}
