package com.lunatech.persistence.generic

import akka.actor.ActorRef
import akka.persistence.{PersistentActor, SnapshotOffer}
import cats.data.NonEmptyList

import scala.reflect.ClassTag

import scala.language.higherKinds

class GenericPersistentActor[S <: GenericState[S, C, E], C[_], E]
  (private var state: S, val persistenceId: String)
  (implicit eTag: ClassTag[E], sTag: ClassTag[S], cTag: ClassTag[C[_]])
  extends PersistentActor {

  override def receiveRecover: Receive = {
    case event: E => updateState(event)
    case SnapshotOffer(_, snapshot: S) => state = snapshot
  }

  override def receiveCommand: Receive = {
    case cTag(command) =>
      state.validateCommand(command)(sender) match {
        case Some(events) => persistAll(events.toList)(updateState)
        case None => ()
      }

    case _ => ()
  }

  private def updateState(event: E): Unit = state = state.processEvent(event)(sender)
}

trait GenericState[S, C[_], E] {
  def validateCommand(command: C[_])(sender: ActorRef): Option[NonEmptyList[E]]
  def processEvent(event: E)(sender: ActorRef): S
}
