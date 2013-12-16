import com.typesafe.sbt.SbtNativePackager.packageArchetype

organization := "org.vastness"

name := "evo2dsim"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.RC2",
  "org.scala-lang" % "scala-swing" % "2.10.3",
  "org.jbox2d" % "jbox2d-library" % "2.2.1.1",
  "com.github.scopt" %% "scopt" % "3.1.0",
  "org.apache.commons" % "commons-math3" % "3.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "io.spray" %% "spray-json" % "1.2.5",
  "org.scalaz" %% "scalaz-core" % "7.1.0-M3",
  "org.vastness" %% "utils" % "0.1",
  "org.scalanlp" %% "breeze" % "0.6-SNAPSHOT",
  "org.encog" % "encog-core" % "3.2.0-SNAPSHOT"
)

packageArchetype.java_application

resolvers ++= Seq(
  "spray" at "http://repo.spray.io/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
