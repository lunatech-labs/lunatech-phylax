# Architecture

The system is a Play/Scala application with persistent actors that
hold the main state of the application. There is one persistent actor
for each bounded context. The front-end consists of two main parts:

- a typical web interface for performing admin duties and retrieving
  information, and
- a restful interface for other applications to retrieve information
  about the team structure.

View actors that subscribe to the persistent actors’ stream of events
will serve queries that are awkward to serve from the main in-memory
model. Each view actor’s model will be specialised for the queries that
it is to serve.

Since Akka actors are typeless (or rather over-generically typed), each
actor will publish an API in its companion object consisting of methods
that take and return specifically-typed values. Messages may only be
sent to an actor from its own companion object — the Meijers principle.

## Tools, libraries and frameworks

- Web framework: Play/Scala, obviously.
- Event sourcing, CQRS: Akka (persitence). One of the research targets.
- Functional Programming: Cats. Provides Free monad support, one of the
  research targets. Cats is more user friendly and has better
  documentation than Scalaz.

## Package structure

- com.lunatech.phylax.&lt;bounded context&gt;: Top-level package for
  each bounded context.
- …model: Contains the classes for the main in-memory model — if it’s
  used in the persistent actor, it goes here.
- …model.query: Model classes that are specific to the view actors, if
  any. May be further subdivided if there are classes that are specific
  to one view actor or a logical group of view actors.
- …actors: The actors live here. May directly use the `model` package.
- …controllers: Play controllers. May directly use the `model` and
  `actors` packages.

## Front-end

HTML pages generated from Twirl templates with Twitter Bootstrap.

## Possible changes in the future

Although it is unlikely that this system will have to scale out to more
than one machine — due to its intended usage, it may be instructive to
split the frontend and/or view actors into their own modules so that
these parts can be made scalable in order to conduct research into the
performance characteristics of such a system.

Switch the front-end to a single-page application using something like
Angular.
