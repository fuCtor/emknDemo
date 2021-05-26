package emkn.w1

import cats.effect.Sync

import java.io.File
import java.nio.file.{Files, StandardOpenOption}
import scala.jdk.CollectionConverters._

trait Storage[F[_], T] {
  def readAll: F[List[T]]

  def write(data: T): F[Unit]
}

final class FileStorage[F[_]: Sync, T: Encoder: Decoder](fileName: String) extends Storage[F, T] {
  private val path = new File(fileName).toPath
  override def readAll: F[List[T]] = Sync[F].blocking {
    if(Files.exists(path)) {
      val lines = Files.readAllLines(path)
      lines.asScala.map(line => Decoder[T].decode(line.getBytes)).toList
    } else {
      List.empty[T]
    }
  }

  override def write(data: T): F[Unit] = Sync[F].blocking {
    val bytes: Array[Byte] = Encoder[T].encode(data)  :+ '\n'
    Files.write(path, bytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
  }
}
