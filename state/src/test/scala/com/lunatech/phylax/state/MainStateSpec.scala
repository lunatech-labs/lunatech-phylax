package com.lunatech.phylax.state

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import cats.data.Xor
import com.lunatech.phylax.model.main.{Employee, Generators}
import com.lunatech.phylax.model.main.TestData._
import com.lunatech.phylax.state.commands.JoinCommand
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MainStateSpec extends TestKit(ActorSystem("MainStateSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with PropertyChecks
  with ScalaFutures {

  private class TestMainState(override val persistenceId: String) extends MainState

  "MainState" should {
    "create an adapter" in {
      val o = Option(MainState(system))

      o shouldBe defined
    }

    "allow an employee to join" in {
      val actorAdapter: ActorAdapter = new ActorAdapter(system.actorOf(Props(new TestMainState("mainstatespec-actor1"))))
      whenReady(actorAdapter.sendCommand(JoinCommand(email, name)) ) {
        case (Xor.Right(Employee(`email`, `name`, _))) => succeed
        case r => fail(s"Return value was $r io. Xor.right(Employee($email,$name,???))")
      }
    }

    "not allow an employee to join twice" in {
      val actorAdapter: ActorAdapter = new ActorAdapter(system.actorOf(Props(new TestMainState("mainstatespec-actor2"))))
      whenReady {
        for {
          r1 <- actorAdapter sendCommand  JoinCommand(email, name)
          r2 <- actorAdapter sendCommand  JoinCommand(email, "Foo Barrio")
        } yield {
          r1 -> r2
        }
      } {
        case (Xor.Right(Employee(`email`, `name`, _)), Xor.Left(e: IllegalStateException)) =>
          e.getMessage shouldBe "Employee already exists"
        case r =>
          fail(s"""Return value was $r io. (Xor.right(Employee($email,$name,???),Xor.left(IllegalStateException("Employee already exists"))""")
      }
    }

    "allow an arbitrary number of employees to join" in {
      var count = 0

      forAll(Generators.nonEmptyListOfEmailAndNameGen) { emailsAndNames =>
        val actorAdapter: ActorAdapter = new ActorAdapter(system.actorOf(Props(new TestMainState(s"mainstatespec-actor3-$count"))))
        count += 1

        val eventualResults: Future[List[Xor[Exception, Employee]]] = Future.traverse(emailsAndNames) { case(email, name) =>
          actorAdapter sendCommand JoinCommand(email, name)
        }

        whenReady(eventualResults) { results =>
          results collect { case Xor.Right(e: Employee) => e } should have size emailsAndNames.size
        }
      }
    }
  }
}
