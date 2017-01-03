package com.lunatech.phylax.state

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import cats.data.{NonEmptyList, Xor}
import com.lunatech.phylax.model.main.{Employee, Generators}
import com.lunatech.phylax.model.main.TestData._
import com.lunatech.phylax.state.commands.JoinCommand
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
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
  with ScalaFutures
  with MockitoSugar {

  private class TestMainState(override val persistenceId: String, state: State) extends MainState(state)

  private val initialState = State(Nil, Nil, Map())

  "MainState" should {
    "create an adapter" in {
      val o = Option(MainState(system))

      o shouldBe defined
    }

    "pass the command to the state" when {
      "given a JoinCommand" in {

        val mockState = mock[State]
        val actorAdapter = system.actorOf(Props(new TestMainState("mainstatespec-actor1", mockState)))

        val command = JoinCommand("foo@example.com", "Foo Bar")

        when(mockState.validateCommand(command)(self)).thenReturn(None)

        actorAdapter ! command

        verify(mockState).validateCommand(command)(self)
      }
    }
  }

  "State" should {
    "allow an employee to join" in {

      val command = JoinCommand(email, name)
      val events = command.events

      initialState.validateCommand(command)(self) shouldBe NonEmptyList.fromList(events)

      val newState = events.foldLeft(initialState) { case (state, event) => state.processEvent(event)(self) }

      expectMsgPF() {
        case Xor.Right(Employee(`email`, `name`, _)) => succeed
      }

      newState.assigned shouldBe empty
      newState.teams shouldBe empty
      newState.unassigned should have size 1
      newState.unassigned.head should have (
        'email (email),
        'name (name)
      )
    }

    "not allow an employee to join twice" when {
      "employee is unassigned" in {
        val newState = initialState.copy(unassigned = List(employee))

        newState.validateCommand(JoinCommand(email, name))(self) shouldBe None

        expectMsgPF() {
          case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee already exists" => succeed
        }
      }

      "employee is assigned" in {
        val newState = initialState.copy(unassigned = List(employee), teams = Map(employee -> Nil))

        newState.validateCommand(JoinCommand(email, name))(self) shouldBe None

        expectMsgPF() {
          case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee already exists" => succeed
        }
      }
    }

    "allow an arbitrary number of employees to join" in {
      forAll(Generators.nonEmptyListOfEmailAndNameGen) { emailsAndNames =>
        val newState = emailsAndNames.foldLeft(initialState) { case (state, (email, name)) =>
            val events = state.validateCommand(JoinCommand(email, name))(self)
            events shouldBe defined
            val intermediateState = events.get.foldLeft(state) { case (s, event) =>
                s.processEvent(event)(self)
            }

            expectMsgPF() {
              case Xor.Right(Employee(`email`, `name`, _)) => succeed
            }

            intermediateState
        }

        newState.unassigned should have size emailsAndNames.size
      }
    }
  }
}
