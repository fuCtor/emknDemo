package emkn.w2

import cats.Eval
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class PersistentRepositorySpec extends AnyFlatSpec with RepositorySpec with Matchers with MockFactory {
  trait Context {
    val storage: Storage[Eval, String] = mock[Storage[Eval, String]]

    val repository = new PersistentRepository[Eval, String, String](storage, identity[String])
  }

  it should "replace item with same id" in new Context {
    (storage.save _).expects(List("a")).returning(Eval.Unit).twice()
    (storage.load _).expects().returning(Eval.now(List("a"))).repeat(3)

    val res = for {
        _ <- repository.put("a", "a")
        _ <- repository.put("a", "a")
        fromRepo <- repository.get("a")
      } yield fromRepo shouldBe Some("a")
    res.value
  }

  it should "return None for unknown id" in new Context {
    (storage.load _).expects().returning(Eval.now(List.empty[String]))

    (for {
        fromRepo <- repository.get("x")
    } yield fromRepo shouldBe None).value
  }


  it should "put and get item by id" in new Context {
    inSequence {
      (storage.load _).expects().returning(Eval.now(List.empty[String]))
      (storage.save _).expects(List("a")).returning(Eval.Unit)
      (storage.load _).expects().returning(Eval.now(List("a")))
    }

    (for {
        _ <- repository.put("a", "a")
        fromRepo <- repository.get("a")
      } yield fromRepo shouldBe Some("a")).value
  }

  it should "receive all items" in new Context {
    val items = List.fill(10)(UUID.randomUUID().toString).sorted

    inSequence {
      (storage.load _).expects().returning(Eval.now(items))
    }

    repository.all().value shouldBe items
  }
}
