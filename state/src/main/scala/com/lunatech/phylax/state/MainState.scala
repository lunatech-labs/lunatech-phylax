package com.lunatech.phylax.state

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

private class MainState extends PersistentActor {
  override val persistenceId: String = "main-state"

  override val receiveRecover: Receive = {
    case _ => ()
  }

  override val receiveCommand: Receive = {
    case _ => ()
  }
}

object MainState {
  def apply(actorSystem: ActorSystem): ActorAdapter = ActorAdapter(actorSystem.actorOf(Props[MainState]))
}

case class ActorAdapter(actorRef: ActorRef) {

}
