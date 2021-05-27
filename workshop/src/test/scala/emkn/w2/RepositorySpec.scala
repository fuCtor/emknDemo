package emkn.w2

import cats.{Monad, ~>}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait RepositorySpec {
  this : AnyFlatSpec with Matchers =>

  def commonRepository[F[_]: Monad, Id: Ordering, Item](repositoryGen: => Repository[F, Id, Item], itemGen: => Item, item2Id: Item => Id, FK: F ~> cats.Id): Unit = {
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    import cats.syntax.traverse._

    it should "replace item with same id" in {
      val repository = repositoryGen
      val itemA = itemGen
      val idA = item2Id(itemA)
      val itemB = itemGen
      val idB = item2Id(itemA)

      FK(
        for {
          _ <- repository.put(idA, itemA)
          _ <- repository.put(idB, itemB)
          fromRepo <- repository.get(idB)
        } yield fromRepo shouldBe Some(itemB)
      )
    }

    it should "return None for unknown id" in {
      val repository = repositoryGen
      val item = itemGen
      val id = item2Id(item)
      FK(
        for {
          fromRepo <- repository.get(id)
        } yield fromRepo shouldBe None
      )
    }


    it should "put and get item by id" in {
      val repository = repositoryGen
      val item = itemGen
      val id = item2Id(item)
      FK(
        for {
          _ <- repository.put(id, item)
          fromRepo <- repository.get(id)
        } yield fromRepo shouldBe Some(item)
      )
    }

    it should "receive all items" in {
      val repository = repositoryGen
      val items = List.fill(10)(itemGen).sortBy(item2Id)

      FK(
        for {
          _ <- items.traverse( item => repository.put(item2Id(item), item))
          fromRepo <- repository.all()
        } yield fromRepo.sortBy(item2Id) shouldBe items
      )
    }
  }

}
