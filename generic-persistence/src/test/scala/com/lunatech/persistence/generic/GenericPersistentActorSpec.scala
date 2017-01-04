package com.lunatech.persistence.generic

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import cats.data.NonEmptyList
import org.scalatest.WordSpecLike
import org.scalatest.mockito.MockitoSugar

class GenericPersistentActorSpec extends TestKit(ActorSystem("GenericPersistentActorSpec"))
  with WordSpecLike
  with ImplicitSender
  with MockitoSugar {

  private class MockCommand[A]
  private sealed trait Event
  private object Event1 extends Event
  private object Event2 extends Event

  private object ValidationDone

  private class MockState(returnedEvents: Option[NonEmptyList[Event]]) extends GenericState[MockState, MockCommand, Event] {
    override def validateCommand(command: MockCommand[_])(sender: ActorRef): Option[NonEmptyList[Event]] = {
      sender ! ValidationDone
      returnedEvents
    }
    override def processEvent(event: Event)(sender: ActorRef): MockState = {
      sender ! event
      this
    }
  }


  "A GenericPersistentActor" should {
    "submit a received command to the state for validation" in {
      val gpa = system.actorOf(Props(new GenericPersistentActor[MockState, MockCommand, Event](new MockState(None), "foo")))

      val command = new MockCommand[Int]

      gpa ! command

      expectMsg(ValidationDone)
    }

    "submit received events for updating" when {
      "there is one event" in {
        val gpa = system.actorOf(Props(new GenericPersistentActor[MockState, MockCommand, Event](new MockState(Some(NonEmptyList(Event1, Nil))), "foo")))

        val command = new MockCommand[Int]

        gpa ! command

        expectMsg(ValidationDone)
        expectMsg(Event1)
      }

      "there are multiple events" in {
        val gpa = system.actorOf(Props(new GenericPersistentActor[MockState, MockCommand, Event](new MockState(Some(NonEmptyList(Event1, List(Event2)))), "foo")))

        val command = new MockCommand[Int]

        gpa ! command

        expectMsg(ValidationDone)
        expectMsg(Event1)
        expectMsg(Event2)
      }
    }
  }
}
