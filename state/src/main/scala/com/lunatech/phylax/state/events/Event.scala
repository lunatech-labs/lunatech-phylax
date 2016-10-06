package com.lunatech.phylax.state.events

sealed trait Event

case class JoinEvent(email: String, name: String) extends Event
