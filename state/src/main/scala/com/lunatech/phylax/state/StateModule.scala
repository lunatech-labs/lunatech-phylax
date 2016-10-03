package com.lunatech.phylax.state

import akka.actor.ActorSystem

class StateModule(actorSystem: ActorSystem) {
  val mainState: ActorAdapter = MainState(actorSystem)
}
