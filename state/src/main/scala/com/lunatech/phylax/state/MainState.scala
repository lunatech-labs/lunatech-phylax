package com.lunatech.phylax.state

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout
import cats.data.Xor
import com.lunatech.phylax.model.main.{Employee, Team}
import com.lunatech.phylax.state.commands.{Command, JoinCommand}
import com.lunatech.phylax.state.events.{Event, JoinEvent}
import org.joda.time.DateTime

import scala.concurrent.Future

private class MainState extends PersistentActor {
  override val persistenceId: String = "main-state"

  override val receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: State) => state = snapshot
  }

  override val receiveCommand: Receive = {
    case command: Command[_] => command match {
      case JoinCommand(email, _) if state.employeeExists(email) =>
        sender ! Xor.left(new IllegalStateException("Employee already exists"))
      case j: JoinCommand =>
        persistAll(j.events)(updateState)
    }

    case _ => ()
  }

  private var state = State(Nil, Nil, Nil)

  private def updateState(event: Event): Unit = {
    event match {
      case JoinEvent(email, name) =>
        val employee = Employee(email, name, DateTime.now)
        state = state.copy(unassigned = employee :: state.unassigned)
        sender ! Xor.right(employee)
    }

  }
}

private case class State(unassigned: List[Employee], assigned: List[Employee], teams: List[Team]) {
  def employeeExists(email: String): Boolean = (unassigned ::: assigned).map(_.email).contains(email)
}

object MainState {
  def apply(actorSystem: ActorSystem): ActorAdapter = new ActorAdapter(actorSystem.actorOf(Props[MainState]))
}

class ActorAdapter private[state] (actorRef: ActorRef) {
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit private val timeout = Timeout(5.seconds)

  def sendCommand[V](command: Command[V]): Future[Xor[Exception, V]] = (actorRef ? command).mapTo[Xor[Exception, V]]
}
