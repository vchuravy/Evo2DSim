    organization in ThisBuild := "org.vastness"

    version in ThisBuild := "1.0-SNAPSHOT"

    scalaVersion in ThisBuild := "2.11.1"

    incOptions := incOptions.value.withNameHashing(true)

    resolvers in ThisBuild ++= Seq(
            "spray" at "http://repo.spray.io/",
            "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
            "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
    )

    libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.1.7" % "test"
        )


    scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")
