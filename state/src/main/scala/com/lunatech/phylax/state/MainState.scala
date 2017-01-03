package com.lunatech.phylax.state

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout
import cats.data.{NonEmptyList, Xor}
import com.lunatech.phylax.model.main.{Employee, Team}
import com.lunatech.phylax.state.commands.{Command, JoinCommand}
import com.lunatech.phylax.state.events.{Event, JoinEvent}
import org.joda.time.DateTime

import scala.concurrent.Future

private class MainState(private var state: State) extends PersistentActor {
  override val persistenceId: String = "main-state"

  override val receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: State) => state = snapshot
  }

  override val receiveCommand: Receive = {
    case command: Command[_] =>
      state.validateCommand(command)(sender) match {
        case Some(events) => persistAll(events.toList)(updateState)
        case None => ()
      }

    case _ => ()
  }

  private def updateState(event: Event): Unit = state = state.processEvent(event)(self)
}

private case class State(unassigned: List[Employee], assigned: List[Employee], teams: Map[Employee, List[Employee]]) {
  def processEvent(event: Event)(sender: ActorRef): State = event match {
    case JoinEvent(email, name) =>
      val employee = Employee(email, name, DateTime.now)
      sender ! Xor.right(employee)
      copy(unassigned = employee :: unassigned)
  }

  def validateCommand(command: Command[_])(sender: ActorRef): Option[NonEmptyList[Event]] = {
    command match {
      case JoinCommand(email, _) if employeeExists(email) =>
        sender ! Xor.left(new IllegalStateException("Employee already exists"))
        None
      case j: JoinCommand =>
        NonEmptyList.fromList(j.events)
    }
  }

  private def employeeExists(email: String): Boolean = (unassigned ::: assigned).map(_.email).contains(email)
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
