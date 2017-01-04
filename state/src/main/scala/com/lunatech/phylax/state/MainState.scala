package com.lunatech.phylax.state

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout
import cats.data.{NonEmptyList, Xor}
import com.lunatech.persistence.generic.{GenericPersistentActor, GenericState}
import com.lunatech.phylax.model.main.{Employee, Team}
import com.lunatech.phylax.state.commands.{Command, JoinCommand, PromoteCommand}
import com.lunatech.phylax.state.events.{Event, JoinEvent, PromoteEvent}
import org.joda.time.DateTime

import scala.concurrent.Future

private class MainState(private var state: State) extends GenericPersistentActor[State, Command, Event](state, "main-state")

private case class State(unassigned: List[Employee], assigned: List[Employee], teams: Map[Employee, List[Employee]]) extends GenericState[State, Command, Event] {
  def processEvent(event: Event)(sender: ActorRef): State = event match {
    case JoinEvent(email, name) =>
      val employee = Employee(email, name, DateTime.now)
      sender ! Xor.right(employee)
      copy(unassigned = employee :: unassigned)

    case PromoteEvent(email) =>
      val employee = getEmployee(email)
      sender ! Xor.right(Team(employee, Nil))
      copy(teams = teams + (employee -> Nil))
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
    }
  }

  private def employeeExists(email: String): Boolean = (unassigned ::: assigned).map(_.email).contains(email)
  private def isManager(email: String): Boolean = teams.keys.map(_.email).toList.contains(email)
  private def getEmployee(email: String): Employee = (unassigned ::: assigned).find(_.email == email).head
}

object MainState {
  def apply(actorSystem: ActorSystem): ActorAdapter = new ActorAdapter(actorSystem.actorOf(Props(new MainState(State(Nil, Nil, Map())))))
}

class ActorAdapter private[state] (actorRef: ActorRef) {
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit private val timeout = Timeout(5.seconds)

  def sendCommand[V](command: Command[V]): Future[Xor[Exception, V]] = (actorRef ? command).mapTo[Xor[Exception, V]]
}
