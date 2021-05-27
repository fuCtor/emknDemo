package emkn.w2

trait Storage[F[_], Item] {
  def load(): F[List[Item]]
  def save(items: List[Item]): F[Unit]
}
