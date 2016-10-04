package com.lunatech.phylax.model.main

import org.scalatest.{Matchers, WordSpec}
import TestData._
import org.scalatest.prop.PropertyChecks

class EmployeeSpec extends WordSpec with Matchers with PropertyChecks {

  "Employee" should {
    "have an email address" in {
      employee.email shouldBe email
    }

    "have a name" in {
      employee.name shouldBe name
    }

    "be unique according to email address only" in {
      forAll { (s: String) =>
        employee shouldBe employee.copy(name = s)
      }
    }
  }
}
