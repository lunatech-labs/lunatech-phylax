# Phylax

[Phylax](https://en.wiktionary.org/wiki/phylax "Wiktionary definition"),
the keeper of records, is Lunatech’s team-composition service: it will
be able to look up an employee’s team manager, so that someone like _Bad
Panda_ can determine who to send annoying emails to.

# Goals

Solving the _Bad Panda_ problem is only a secondary goal of this
project. The primary goals are to serve as a platform for researching,
studying and validating certain techniques. For now the techniques to be
researched are:

- event sourcing and CQRS with Akka persistence,
- [Free monads]
  (https://en.wikipedia.org/wiki/Monad%5F%28functional%5Fprogramming%29#Free%5Fmonads
  "Wikipedia entry") and
- [DDD] (https://en.wikipedia.org/wiki/Domain-driven_design "Wikipedia entry")

The idea is to determine whether it’s feasible to build an application
using these techniques and whether they provide any kind of advantage.

# Project structure

Product owner and tech lead: Francisco Canedo.

Development team: anyone willing to participate (in their study time).

# Participation

Anyone willing to spend time on the project (solo or in groups) is
welcome to contribute. Simply pick a story from the [backlog]
(https://github.com/lunatech-labs/phylax/milestone/1), assign it
to yourself and start working on a branch. Contributions will only be
merged if they implement a backlog item or contain only simple
refactorings, and have been reviewed according to the [review check
list] (review-checklist.md) by someone who did not participate in the
development of the feature.

To have a new feature added to the backlog, simply send a request to the
product owner.

Versions of the system that replace one or more of the techniques with
other techniques, so that comparisons can be made, are encouraged.
Branches in the main repository may be used for this purpose.
