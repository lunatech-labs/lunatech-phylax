package com.lunatech.phylax.model.main

import org.joda.time.DateTime

case class Employee(email: String, name: String, since: DateTime) {
  // email address is what makes an employee unique
  override def equals(that: scala.Any): Boolean = that match {
    case e @ Employee(thatEmail, _, _) => (this eq e) || (thatEmail == email)
    case _ => false
  }

  override def hashCode(): Int = email.hashCode
}
