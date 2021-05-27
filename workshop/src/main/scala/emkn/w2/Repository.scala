package emkn.w2

trait Repository[F[_], Id, Item] {
  def put(id: Id, item: Item): F[Unit]
  def get(id: Id): F[Option[Item]]
  def all(): F[List[Item]]
}
