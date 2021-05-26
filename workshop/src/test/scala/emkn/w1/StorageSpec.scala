package emkn.w1

import cats.effect.IO
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files

class StorageSpec extends AnyFlatSpec with Matchers with MockFactory with BeforeAndAfterEach {
  val file = new File("TempFile.txt")

  override def afterEach(): Unit = {
    file.delete()
  }

  def withContext[T](f: (Converter[Data], Storage[IO, Data]) => IO[T]): T = {
    import cats.effect.unsafe.implicits.global
    implicit val converter: Converter[Data] = mock[Converter[Data]]
    val storage = new FileStorage[IO, Data](file.getAbsolutePath)

    f(converter, storage).unsafeRunSync()
  }

  "FileStorage" should "write in file" in withContext { (converter, storage) =>
    converter.encode _ expects Data("foo", "bar") returning "data".getBytes
    storage.write(Data("foo", "bar")).map({ _ =>
      new String(Files.readAllBytes(file.toPath))  shouldBe "data\n"
    })
  }

  "FileStorage" should "read from file" in withContext { (converter, storage) =>
    converter.encode _ expects Data("foo", "bar") returning "data".getBytes
    converter.decode _ expects where { (data: Array[Byte]) =>
      new String(data) == "data"
    } returning Data("bar", "foo")

    for {
      _ <- storage.write(Data("foo", "bar"))
      seq <- storage.readAll
    } yield seq shouldBe Seq(Data("bar", "foo"))
  }
}
