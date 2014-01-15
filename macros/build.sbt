organization := "org.vastness"

name := "utils"

version := "0.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)