package emkn.w1

trait Encoder[T] {
  def encode(item: T): Array[Byte]
}

trait Decoder[T] {
  def decode(raw: Array[Byte]): T
}

object Encoder {
  def apply[T](implicit inst: Encoder[T]): Encoder[T] = inst
}

object Decoder {
  def apply[T](implicit inst: Decoder[T]): Decoder[T] = inst
}

trait Converter[T] extends Encoder[T] with Decoder[T]

final class TsvDataConverter extends Converter[Data] {
  override def decode(raw: Array[Byte]): Data = {
    val parts = new String(raw).split('\t')
    Data(parts(0), parts(1))
  }

  override def encode(item: Data): Array[Byte] = s"${item.key}\t${item.value}".getBytes
}