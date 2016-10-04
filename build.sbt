name := "phylax"

version := "1.0-SNAPSHOT"

val commonSettings = List(
  scalaVersion := "2.11.8"
)

val playScalatestVersion = "2.2.6"
val scalatestScalacheckVersion = "1.12.5"

val playJodaTimeVersion = "2.9.2"

val scalatest = Seq(
  "org.scalatest" %% "scalatest" % playScalatestVersion % "test",
  "org.scalacheck" %% "scalacheck" % scalatestScalacheckVersion % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(commonSettings: _*)
  .dependsOn(state, model)
  .aggregate(model, state)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

lazy val state = project.settings(commonSettings: _*).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-persistence" % "2.4.4",
    "com.typesafe.akka" %% "akka-testkit" % "2.4.4" % "test"
  ) ++ scalatest
).dependsOn(model)

lazy val model = project.settings(commonSettings: _*).settings(
  libraryDependencies ++= scalatest :+
    "joda-time" % "joda-time" % playJodaTimeVersion
)
