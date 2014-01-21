    organization in ThisBuild := "org.vastness"

    version in ThisBuild := "1.0-SNAPSHOT"

    scalaVersion in ThisBuild := "2.10.3"

    resolvers in ThisBuild ++= Seq(
            "spray" at "http://repo.spray.io/",
            "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
            "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
    )

    scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")
