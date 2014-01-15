import sbt._

object Evo2DSimBuild extends Build {
    lazy val root = project in file(".") aggregate(core, analyzer)

    lazy val core = project in file("core")
    lazy val analyzer = project in file("analyzer") dependsOn core
}