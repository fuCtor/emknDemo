package emkn.w2

trait Encoder[T] {
  def encode(item: T): Array[Byte]
}

object Encoder {
  implicit val stringEncoder: Encoder[String] = _.getBytes
  def apply[T](implicit inst: Encoder[T]): Encoder[T] = inst
}
