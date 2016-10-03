name := "phylax"

version := "1.0-SNAPSHOT"

val commonSettings = List(
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(commonSettings: _*).dependsOn(state).aggregate(state)

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
    "com.typesafe.akka" %% "akka-testkit" % "2.4.4" % "test",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
)
