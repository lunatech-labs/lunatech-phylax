package com.lunatech.phylax.state

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import cats.data.{NonEmptyList, Xor}
import com.lunatech.persistence.generic.{GenericPersistentActor, GenericState}
import com.lunatech.phylax.model.main.{Employee, Team}
import com.lunatech.phylax.state.commands.{AddCommand, Command, JoinCommand, PromoteCommand}
import com.lunatech.phylax.state.events.{AddEvent, Event, JoinEvent, PromoteEvent}
import org.joda.time.DateTime

import scala.concurrent.Future

private class MainState(private var state: State) extends GenericPersistentActor[State, Command, Event](state, "main-state")

private case class State(unassigned: Set[Employee], assigned: Set[Employee], teams: Map[Employee, List[Employee]]) extends GenericState[State, Command, Event] {
  def processEvent(event: Event)(sender: ActorRef): State = event match {
    case JoinEvent(email, name) =>
      val employee = Employee(email, name, DateTime.now)
      sender ! Xor.right(employee)
      copy(unassigned = unassigned + employee)

    case PromoteEvent(email) =>
      val employee = getEmployee(email)
      sender ! Xor.right(Team(employee, Nil))
      copy(teams = teams + (employee -> Nil))

    case AddEvent(managerEmail, employeeEmail) =>
      val manager = getEmployee(managerEmail)
      val employee = getEmployee(employeeEmail)
      val newTeam = employee :: teams(manager)

      sender ! Xor.right(Team(manager, newTeam))
      copy(unassigned = unassigned - employee, assigned = assigned + employee, teams = teams.updated(manager, newTeam))
  }

  def validateCommand(command: Command[_])(sender: ActorRef): Option[NonEmptyList[Event]] = {
    command match {
      case JoinCommand(email, _) if employeeExists(email) =>
        sender ! Xor.left(new IllegalStateException("Employee already exists"))
        None
      case j: JoinCommand =>
        NonEmptyList.fromList(j.events)

      case PromoteCommand(email) if !employeeExists(email) =>
        sender ! Xor.left(new IllegalStateException("Employee does not exist"))
        None
      case PromoteCommand(email) if isManager(email) =>
        sender ! Xor.left(new IllegalStateException("Employee is already a manager"))
        None
      case p: PromoteCommand =>
        NonEmptyList.fromList(p.events)

      case AddCommand(_, employee) if !employeeExists(employee) =>
        sender ! Xor.left(new IllegalStateException("Employee does not exist"))
        None
      case AddCommand(manager, _) if !employeeExists(manager) =>
        sender ! Xor.left(new IllegalStateException("Manager does not exist"))
        None
      case AddCommand(manager, _) if !isManager(manager) =>
        sender ! Xor.left(new IllegalStateException("Team does not exist"))
        None
      case AddCommand(manager, employee) if isEmployeesManager(employee, manager) =>
        sender ! Xor.left(new IllegalStateException("Proposed team member is already the manager’s manager (possibly transitively)"))
        None
      case a: AddCommand =>
        NonEmptyList.fromList(a.events)
    }
  }

  private def employeeExists(email: String): Boolean = (unassigned ++ assigned).map(_.email).contains(email)
  private def isManager(email: String): Boolean = teams.keys.map(_.email).toList.contains(email)
  private def getEmployee(email: String): Employee = (unassigned ++ assigned).find(_.email == email).head

  private def getManager(email: String): Option[Employee] = {
    teams.collect {
      case (manager, employees) if employees.contains(Employee(email, "", DateTime.now)) => manager
    }.headOption
  }

  private def isEmployeesManager(managerToCheck: String, employee: String): Boolean = {
    getManager(employee) match {
      case None => false
      case Some(manager) if manager.email != managerToCheck => isEmployeesManager(manager.email, employee)
      case _ => true
    }
  }
}

object MainState {
  def apply(actorSystem: ActorSystem): ActorAdapter = new ActorAdapter(actorSystem.actorOf(Props(new MainState(State(Set(), Set(), Map())))))
}

class ActorAdapter private[state] (actorRef: ActorRef) {
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit private val timeout = Timeout(5.seconds)

  def sendCommand[V](command: Command[V]): Future[Xor[Exception, V]] = (actorRef ? command).mapTo[Xor[Exception, V]]
}
