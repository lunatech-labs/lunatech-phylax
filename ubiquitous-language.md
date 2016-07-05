# Phylax’s ubiquitous language

## Employee teams

### Employee

A natural person who works for Lunatech.

### Team manager

An _employee_ who is the head of a team.

### Team

A group of _employees_, exactly one of which is a _team manager_.

### promote

Promotes an _employee_ to the status of _team manager_. This implicitly
creates a new _team_ with the target _employee_ as its _team manager_
and 0 members.

### demote

Demotes a _team manager_ back to regular _employee_. This implicitly
disbands their team: the _employee_ members of that team will no longer
be in a team.

### join

Adds a new person to the system as an _employee_. This person must have
a valid lunatech.com email address.

### leave

Removes an _employee_ from the system. Implicitly removes the _employee_
from the _team_ that they belong to, if any. If this _employee_ is _team
manager_ their _team_ is also implicitly disbanded.

### add

Adds an _employee_ to an existing _team_. The _employee_ may not be in
another team and this may not create a circular relationship between two
or more _team managers_.

### remove

Removes an _employee_ from a _team_. The _employee_ may not be the
_team_’s _team manager_.

### transfer

Basically a sequence of _remove_ and _add_ actions for the same
_employee_. The old and new _team_ may not be the same.
