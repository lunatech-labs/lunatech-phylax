package com.lunatech.phylax.model.main

import org.scalatest.{Matchers, WordSpec}
import TestData._
import org.joda.time.DateTime

class TeamSpec extends WordSpec with Matchers {

  "Team" should {
    "have a manager" in {
      team.manager shouldBe employee
    }

    "not allow a manager to be in his own team" in {
      an[IllegalArgumentException] shouldBe thrownBy(Team(employee, List(TeamMember(employee, DateTime.now)), DateTime.now))
      an[IllegalArgumentException] shouldBe thrownBy(team.copy(members = List(TeamMember(employee, DateTime.now))))
    }
  }

}
