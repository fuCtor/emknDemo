package emkn.w2

import cats.effect.IO
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files

class FileStorageSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach {
  import cats.effect.unsafe.implicits.global
  import scala.jdk.CollectionConverters._
  val file = new File("TempFile.txt")

  override def afterEach(): Unit = {
    file.delete()
  }

  it should "save data to file" in {
    val storage = new FileStorage[IO, String](file)
    storage.save(List("foo", "bar")).unsafeRunSync()
    Files.readAllLines(file.toPath).asScala.toList shouldBe List("foo", "bar")
  }

  it should "load data from file" in {
    val storage = new FileStorage[IO, String](file)
    storage.save(List("foo", "bar")).flatMap(
      _ => storage.load()
    ).unsafeRunSync() shouldBe List("foo", "bar")
  }

}
