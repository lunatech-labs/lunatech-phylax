package com.lunatech.phylax.state.events

sealed trait Event

case class JoinEvent(email: String, name: String) extends Event
case class PromoteEvent(email: String) extends Event
case class AddEvent(manager: String, employee: String) extends Event
