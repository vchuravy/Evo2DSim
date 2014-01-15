name := "evo2dsim-core"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0.1-SNAP4",
  "org.scala-lang" % "scala-swing" % scalaVersion.value,
  "org.jbox2d" % "jbox2d-library" % "2.2.1.1",
  "com.github.scopt" %% "scopt" % "3.2.0",
  "org.apache.commons" % "commons-math3" % "3.2",
  "org.apache.commons" % "commons-compress" % "1.6",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "io.spray" %% "spray-json" % "1.2.5",
  "org.scalaz" %% "scalaz-core" % "7.1.0-M4",
  "org.spire-math" %% "spire" % "0.7.3"
)

packageArchetype.java_application
