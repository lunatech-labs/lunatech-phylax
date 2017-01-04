package com.lunatech.phylax.model.main

import org.joda.time.DateTime

case class Team(manager: Employee, members: List[Employee]) {
  require(!(members contains manager), "A team’s manager may not also be a member of the team")
}
