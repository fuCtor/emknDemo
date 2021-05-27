package emkn.w2

import cats.{Eval, Id}
import cats.arrow.FunctionK
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class InMemoryRepositorySpec extends AnyFlatSpec with RepositorySpec with Matchers {

  val fk = new FunctionK[Eval, Id] {
    override def apply[A](fa: Eval[A]): Id[A] = fa.value
  }

  it should behave like commonRepository(new InMemoryRepository[Eval, String, String], UUID.randomUUID().toString, (k: String) => k, fk)
}
