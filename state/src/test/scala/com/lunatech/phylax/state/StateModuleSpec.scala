package com.lunatech.phylax.state

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{Matchers, WordSpecLike}

class StateModuleSpec extends TestKit(ActorSystem("statemodulespec")) with WordSpecLike with Matchers {
  "StateModule" should {
    "provide a MainStateAdapter after initializing" in {
      val stateModule = new StateModule(system)

      Option(stateModule.mainState) shouldBe defined
    }
  }
}
