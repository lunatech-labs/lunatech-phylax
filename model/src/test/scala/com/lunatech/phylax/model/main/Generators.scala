package com.lunatech.phylax.model.main

import org.joda.time.DateTime
import org.scalacheck._
import org.scalacheck.Gen._

object Generators {

  def nameToEmail(name: String): String = {
    name.replaceAll("\\s", ".").toLowerCase + "@example.com"
  }

  def toProperCase(s: String): String = s"${s.head.toString.toUpperCase}${s.tail.toString.toLowerCase}"

  val nonEmptyAlphaStr = "nonEmptyAlphaStr" |: (for {
    chars <- nonEmptyListOf(alphaChar)
    string = chars.mkString
  } yield string)

  val nameGen: Gen[String] = "nameGen" |: (for {
    givenName <- nonEmptyAlphaStr
    surname <- nonEmptyAlphaStr
    fullName = s"${toProperCase(givenName)} ${toProperCase(surname)}"
  } yield fullName)

  val nonEmptyListOfEmailAndNameGen: Gen[List[(String, String)]] = "nonEmptyListOfEmailAndNameGen" |: (for {
    names <- nonEmptyListOf(nameGen)
    deduped = names.toSet.toList
    emailsAndNames = deduped.map { name => nameToEmail(name) -> name }
  } yield emailsAndNames)

  val emailAndNameGen: Gen[(String, String)] = "emailAndNameGen" |: (for {
    name <- nameGen
    email = nameToEmail(name)
    emailAndName = nameToEmail(name) -> name
  } yield emailAndName)

  val employeeGen: Gen[Employee] = "employeeGen" |: (for {
    (email, name) <- emailAndNameGen
    employee = Employee(email, name, DateTime.now)
  } yield employee)

  implicit val arbitraryEmployee: Arbitrary[Employee] = Arbitrary(employeeGen)
}
