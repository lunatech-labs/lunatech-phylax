package com.lunatech.phylax.model.main

import org.joda.time.DateTime

object TestData {
  val email = "foo.bar@example.com"
  val name = "Foo Bar"
  val employee = Employee(email, name, DateTime.now)

  val team = Team(employee, Nil)
}
