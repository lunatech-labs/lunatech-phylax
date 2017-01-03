name := "phylax"

version := "1.0-SNAPSHOT"

val commonSettings = List(
  scalaVersion := "2.11.8",
  libraryDependencies += "org.typelevel" %% "cats" % "0.7.0"
)

val scalatestVersion = "3.0.0"
val scalatestScalacheckVersion = "1.13.1"

val playJodaTimeVersion = "2.9.2"

val scalatest = Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "org.scalacheck" %% "scalacheck" % scalatestScalacheckVersion % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
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
).dependsOn(model, model % "test->test")

lazy val model = project.settings(commonSettings: _*).settings(
  libraryDependencies ++= scalatest ++ Seq(
    "joda-time" % "joda-time" % playJodaTimeVersion,
    "org.joda" % "joda-convert" % "1.2"
  )
)
