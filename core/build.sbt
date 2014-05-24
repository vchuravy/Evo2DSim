name := "evo2dsim-core"

val truezipVersion = "7.7.6"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing" % "1.0.1",
  "org.jbox2d" % "jbox2d-library" % "2.2.1.1",
  "com.github.scopt" %% "scopt" % "3.2.0",
  "org.apache.commons" % "commons-math3" % "3.3",
  "com.google.code.findbugs" % "annotations" % "2.0.3",
  "de.schlichtherle.truezip" %	"truezip" %	truezipVersion,
  "de.schlichtherle.truezip" %	"truezip-kernel" % truezipVersion,
  "de.schlichtherle.truezip" %	"truezip-file" % truezipVersion,
  "de.schlichtherle.truezip" %  "truezip-driver-tar" % truezipVersion,
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",
  "io.spray" %% "spray-json" % "1.2.6",
  "org.scalaz" %% "scalaz-core" % "7.1.0-M7",
  "org.spire-math" %% "spire" % "0.7.4"
)

packageArchetype.java_application
