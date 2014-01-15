import com.typesafe.sbt.SbtNativePackager.packageArchetype

name := "evo2dsim"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.RC2",
  "org.scala-lang" % "scala-swing" % "2.10.3",
  "org.jbox2d" % "jbox2d-library" % "2.2.1.1",
  "com.github.scopt" %% "scopt" % "3.1.0",
  "org.apache.commons" % "commons-math3" % "3.2",
  "org.apache.commons" % "commons-compress" % "1.6",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "io.spray" %% "spray-json" % "1.2.5",
  "org.scalaz" %% "scalaz-core" % "7.1.0-M4",
  "org.vastness" %% "utils" % "0.1",
  "org.spire-math" %% "spire" % "0.7.1"
)

packageArchetype.java_application
