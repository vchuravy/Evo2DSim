    organization := "org.vastness"

    version := "1.0-SNAPSHOT"

    scalaVersion := "2.10.3"

    resolvers ++= Seq(
            "spray" at "http://repo.spray.io/",
            "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
            "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
    )

    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
