package com.lunatech.phylax.model.main

import org.joda.time.DateTime

private object TestData {
  val email = "foo@example.com"
  val name = "Foo Bar"
  val employee = Employee(email, name)

  val team = Team(employee, Nil, DateTime.now)
}
