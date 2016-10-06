package com.lunatech.phylax.model.main

import org.scalatest.{Matchers, WordSpec}
import TestData._
import org.scalatest.prop.PropertyChecks
import com.lunatech.phylax.model.main.Generators._
import org.scalacheck.Gen

class EmployeeSpec extends WordSpec with Matchers with PropertyChecks {

  "Employee" should {
    "have an email address" in {
      employee.email shouldBe email
    }

    "have a name" in {
      employee.name shouldBe name
    }

    "foo" in {
      forAll(Gen.nonEmptyListOf(Gen.alphaStr retryUntil { _.nonEmpty })) { s =>
        s shouldBe s
      }
    }

    "be unique according to email address only" in {
      forAll(employeeGen) { employee =>
        employee shouldBe employee.copy(name = employee.name + "foo")
      }
    }
  }
}
