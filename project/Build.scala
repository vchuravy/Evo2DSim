import sbt._

object MyBuild extends Build {

  lazy val root = Project("root", file("."))
    .dependsOn(scala_utils)

  lazy val scala_utils = RootProject(uri("git://github.com/wallnuss/scala-utils.git"))
}