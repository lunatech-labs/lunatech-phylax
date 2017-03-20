package com.lunatech.phylax.state

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import cats.data.{NonEmptyList, Xor}
import com.lunatech.phylax.model.main.{Employee, Generators, Team}
import com.lunatech.phylax.model.main.TestData._
import com.lunatech.phylax.state.commands.{AddCommand, JoinCommand, PromoteCommand}
import com.lunatech.phylax.state.events.{AddEvent, Event}
import org.joda.time.DateTime
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpecLike}

class MainStateSpec extends TestKit(ActorSystem("MainStateSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with PropertyChecks {

  private class TestMainState(override val persistenceId: String, state: State) extends MainState(state)

  private val emptyState = State(Set(), Set(), Map())

  "MainState" should {
    "create an adapter" in {
      val o = Option(MainState(system))

      o shouldBe defined
    }
  }

  "State, " when {
    "an employee joins" should {
      "allow it" in {

        val command = JoinCommand(email, name)
        val events = command.events

        events.size shouldBe > (0)

        emptyState.validateCommand(command)(self) shouldBe NonEmptyList.fromList(events)

        val newState = processEvents(emptyState)(NonEmptyList.fromList(events))

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
          val newState = emptyState.copy(unassigned = Set(employee))

          newState.validateCommand(JoinCommand(email, name))(self) shouldBe None

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee already exists" => succeed
          }
        }

        "employee is assigned" in {
          val newState = emptyState.copy(unassigned = Set(employee), teams = Map(employee -> Nil))

          newState.validateCommand(JoinCommand(email, name))(self) shouldBe None

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee already exists" => succeed
          }
        }
      }

      "allow an arbitrary number of employees to join" in {
        forAll(Generators.nonEmptyListOfEmailAndNameGen) { emailsAndNames =>
          val newState = emailsAndNames.foldLeft(emptyState) { case (state, (email, name)) =>
            val events = state.validateCommand(JoinCommand(email, name))(self)

            events shouldBe defined

            val intermediateState = processEvents(state)(events)

            expectMsgPF() {
              case Xor.Right(Employee(`email`, `name`, _)) => succeed
            }

            intermediateState
          }

          newState.unassigned should have size emailsAndNames.size
        }
      }
    }

    "an employee is promoted" should {
      "allow it" when {
        "the employee is unassigned and there are no teams" in {
          val newState = emptyState.copy(unassigned = Set(employee))

          val events = newState.validateCommand(PromoteCommand(employee.email))(self)

          events shouldBe defined

          val endState = processEvents(newState)(events)

          expectMsg(Xor.Right(Team(employee, Nil)))

          endState.unassigned should contain (employee)
          endState.assigned shouldBe empty
          val possibleNewTeam = endState.teams.get(employee)
          possibleNewTeam shouldBe  defined
          val newTeam = possibleNewTeam.get
          newTeam shouldBe empty
        }

        "the employee is assigned" in {

          val manager = Employee("buzz@example.com", "Buzz Lightyear", DateTime.now)
          val newState = emptyState.copy(unassigned = Set(manager), assigned = Set(employee), teams = Map(manager -> Nil))

          val events = newState.validateCommand(PromoteCommand(employee.email))(self)

          events shouldBe defined

          val endState = processEvents(newState)(events)

          expectMsg(Xor.Right(Team(employee, Nil)))

          endState.assigned should contain (employee)
          endState.unassigned should contain (manager)
          val possibleNewTeam = endState.teams.get(employee)
          possibleNewTeam shouldBe  defined
          val newTeam = possibleNewTeam.get
          newTeam shouldBe empty
        }
      }

      "not allow it" when {
        "the employee does not exist" in {
          val events = emptyState.validateCommand(PromoteCommand(employee.email))(self)

          events shouldBe empty

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee does not exist" => succeed
          }
        }

        "the employee is already a manager" in {
          val newState = emptyState.copy(unassigned = Set(employee), teams = Map(employee -> Nil))

          val events = newState.validateCommand(PromoteCommand(employee.email))(self)

          events shouldBe empty

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee is already a manager" => succeed
          }
        }
      }
    }

    "an employee is added to a team" should {
      "not allow it" when {
        "the employee does not exist" in {
          val manager = Employee("buzz@example.com", "Buzz Lightyear", DateTime.now)
          val startState = emptyState.copy(unassigned = Set(manager), teams = Map(manager -> Nil))

          val events = startState.validateCommand(AddCommand(manager.email, employee.email))(self)

          events shouldBe empty

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Employee does not exist" => succeed
          }
        }

        "the manager does not exist" in {
          val manager = Employee("buzz@example.com", "Buzz Lightyear", DateTime.now)
          val startState = emptyState.copy(unassigned = Set(employee))

          val events = startState.validateCommand(AddCommand(manager.email, employee.email))(self)

          events shouldBe empty

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Manager does not exist" => succeed
          }
        }

        "the team does not exist" in {
          val manager = Employee("buzz@example.com", "Buzz Lightyear", DateTime.now)
          val startState = emptyState.copy(unassigned = Set(employee, manager))

          val events = startState.validateCommand(AddCommand(manager.email, employee.email))(self)

          events shouldBe empty

          expectMsgPF() {
            case Xor.Left(e: IllegalStateException) if e.getMessage == "Team does not exist" => succeed
          }
        }
      }

      "allow it" when {
        "the team exists but is empty" in {
          val manager = Employee("buzz@example.com", "Buzz Lightyear", DateTime.now)
          val startState = emptyState.copy(unassigned = Set(employee, manager), teams = Map(manager -> Nil))

          val events = startState.validateCommand(AddCommand(manager.email, employee.email))(self)

          events shouldBe Some(NonEmptyList(AddEvent(manager.email, employee.email), Nil))

          val endState = processEvents(startState)(events)

          endState.unassigned should have size 1
          endState.assigned should have size 1
          endState.teams should have size 1

          expectMsg(Xor.Right(Team(manager, List(employee))))
        }

        "the team exists and is not empty" in {
          val manager = Employee("buzz@example.com", "Buzz Lightyear", DateTime.now)
          val employee2 = Employee("stan@example.com", "Stan Laurel", DateTime.now)
          val startState = emptyState.copy(unassigned = Set(employee, manager), assigned = Set(employee2), teams = Map(manager -> List(employee2)))

          val events = startState.validateCommand(AddCommand(manager.email, employee.email))(self)

          events shouldBe Some(NonEmptyList(AddEvent(manager.email, employee.email), Nil))

          val endState = processEvents(startState)(events)

          endState.unassigned should have size 1
          endState.assigned should have size 2
          endState.teams should have size 1

          expectMsg(Xor.Right(Team(manager, List(employee, employee2))))
        }
      }
    }
  }

  private def processEvents(state: State)(possibleEvents: Option[NonEmptyList[Event]]): State = {
    possibleEvents.map { events =>
      events.foldLeft(state) { case (s, e) =>
        s.processEvent(e)(self)
      }
    }.getOrElse(state)
  }
}
