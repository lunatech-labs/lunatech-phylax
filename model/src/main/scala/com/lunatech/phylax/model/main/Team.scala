package com.lunatech.phylax.model.main

import org.joda.time.DateTime

case class Team(manager: Employee, members: List[TeamMember], since: DateTime) {
  require(!(members.map(_.employee) contains manager), "A team’s manager may not also be a member of the team")
}
