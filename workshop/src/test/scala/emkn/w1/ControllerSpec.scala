package emkn.w1

import cats.Eval
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}

class ControllerSpec extends AnyFlatSpec with ScalaFutures with Matchers with MockFactory {

  import Request._

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  trait Context {
    val repo: Repository[Eval, Data, String] = mock[Repository[Eval, Data, String]]
    implicit val printer: Printer[Eval] = mock[Printer[Eval]]
    val controller = new DataController(repo)
  }

  "DataController" should "add new item" in new Context {
    repo.put _ expects ("a", Data("a", "b")) returning Eval.Unit
    controller(Add(Data("a", "b"))).value
  }

  "DataController" should "print item by key" in new Context {
    repo.get _ expects "a" returning Eval.now(Option(Data("a", "b")))
    printer.println _ expects Data("a", "b") returning Eval.Unit
    controller(Get("a")).value
  }

  "DataController" should "print not found for wrong key" in new Context {
    repo.get _ expects "a" returning Eval.now(None)
    printer.println _ expects "Not Found" returning Eval.Unit
    controller(Get("a")).value
  }

  "DataController" should "print all items" in new Context {
    (repo.all _).expects() returning Eval.now(List(Data("a", "b"), Data("b", "a")))
    inSequence {
      printer.println _ expects Data("a", "b") returning Eval.Unit
      printer.println _ expects Data("b", "a") returning Eval.Unit
    }

    controller(PrintAll).value
  }

  "DataController" should "print grouped by value" in new Context {
    (repo.all _).expects() returning Eval.now(List(Data("a", "b"), Data("c", "b"), Data("b", "a")))
    inSequence {
      printer.println _ expects List(Data("b", "a")) returning Eval.Unit
      printer.println _ expects List(Data("a", "b"), Data("c", "b")) returning Eval.Unit
    }

    controller(GroupByValue).value
  }
}
