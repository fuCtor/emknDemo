package emkn.w2


import cats.effect.kernel.Sync

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardOpenOption}

class FileStorage[F[_]: Sync, Item: Encoder : Decoder](file: File) extends Storage[F, Item] {
  import scala.jdk.CollectionConverters._
  override def load(): F[List[Item]] = Sync[F].blocking {
    Files.readAllLines(file.toPath, StandardCharsets.UTF_8).asScala.toList
      .map( line =>
        Decoder[Item].decode(line.getBytes)
      )
  }

  override def save(items: List[Item]): F[Unit] = Sync[F].blocking {
    val lines = items.map(item => new String(Encoder[Item].encode(item))).asJava
    Files.write(file.toPath, lines, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}
