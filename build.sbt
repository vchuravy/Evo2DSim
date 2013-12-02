organization := "org.vastness"

name := "Evo2DSim"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.RC2",
  "org.jbox2d" % "jbox2d-library" % "2.2.1.1",
  "com.intellij" % "forms_rt" % "7.0.3",
  "com.github.scopt" %% "scopt" % "3.1.0",
  "org.apache.commons" % "commons-math3" % "3.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "io.spray" %% "spray-json" % "1.2.5",
  "org.scalaz" %% "scalaz-core" % "7.1.0-M3",
  "org.vastness" %% "utils" % "0.1"
)

resolvers += "spray" at "http://repo.spray.io/"