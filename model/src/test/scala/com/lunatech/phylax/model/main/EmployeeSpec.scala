package com.lunatech.phylax.model.main

import org.scalatest.{Matchers, WordSpec}
import TestData._
import org.scalatest.prop.PropertyChecks
import com.lunatech.phylax.model.main.Generators._

class EmployeeSpec extends WordSpec with Matchers with PropertyChecks {

  "Employee" should {
    "have an email address" in {
      employee.email shouldBe email
    }

    "have a name" in {
      employee.name shouldBe name
    }

    "be unique according to email address only" in {
      forAll(employeeGen) { employee =>
        employee shouldBe employee.copy(name = employee.name + "foo")
      }
    }
  }
}
