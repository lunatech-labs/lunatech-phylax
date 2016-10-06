package com.lunatech.phylax.state.commands

import com.lunatech.phylax.model.main.Employee
import com.lunatech.phylax.state.events.{Event, JoinEvent}

sealed trait Command[V] {
  def events: List[Event]
}

case class JoinCommand(email: String, name: String) extends Command[Employee] {
  override def events: List[Event] = List(JoinEvent(email, name))
}
