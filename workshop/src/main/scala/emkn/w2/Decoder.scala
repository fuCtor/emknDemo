package emkn.w2

trait Decoder[T] {
  def decode(data: Array[Byte]): T
}

object Decoder {
  implicit val stringDecoder: Decoder[String] = new String(_)
  def apply[T](implicit inst: Decoder[T]): Decoder[T] = inst
}