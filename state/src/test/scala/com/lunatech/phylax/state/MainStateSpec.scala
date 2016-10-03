package com.lunatech.phylax.state

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class MainStateSpec extends TestKit(ActorSystem("MainStateSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  "MainState" should {
    "create an adapter" in {
      val o = Option(MainState(system))

      o shouldBe defined
    }
  }

}
