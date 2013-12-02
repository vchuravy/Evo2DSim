organization := "org.vastness"

name := "evo2dsim-analyzer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"
  "org.scalanlp" %% "breeze-viz" % "0.5.2",
  "org.vastness" %% "evo2dsim" % "1.0-SNAPSHOT"
)