package emkn.w1

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RepositorySpec extends AnyFlatSpec with Matchers with MockFactory {

  trait ContextA {
    val repo = new MemoryDataRepository[IO]()
  }

  trait ContextB {
    val storage = mock[Storage[IO, Data]]
    val repo = new PersistentDataRepository(storage)
  }

  "MemoryRepository" should "store item" in new ContextA {
    repo.put(Data("foo", "bar")).map(_ => succeed)
  }

  "MemoryRepository" should "return stored item" in new ContextA {
    val f = for {
      _ <- repo.put(Data("foo", "bar"))
      item <- repo.get("foo")
    } yield item.map(_.key)

    f.unsafeRunSync() shouldBe Some("foo")
  }

  "MemoryRepository" should "None for unknown key" in new ContextA {
    repo.get("foo").unsafeRunSync() shouldBe empty
  }

  "PersistentDataRepository" should "store item via storage" in new ContextB {
    storage.write _ expects Data("foo", "bar") returning IO.unit
    repo.put(Data("foo", "bar")).map(_ => succeed).unsafeRunSync()
  }

  "PersistentDataRepository" should "return item stored item via storage" in new ContextB {
    storage.write _ expects Data("foo", "bar") returning IO.unit
    (storage.readAll _).expects() returning IO(List(Data("foo", "bar")))

    val f = for {
      _ <- repo.put(Data("foo", "bar"))
      item <- repo.get("foo")
    } yield item.map(_.key)

    f.unsafeRunSync() shouldBe Some("foo")
  }

  "PersistentDataRepository" should "None for unknown key" in new ContextB {
    (storage.readAll _).expects() returning IO(List.empty)

    repo.get("foo").unsafeRunSync() shouldBe empty
  }

}
