import sbt._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName = "walldee"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    "com.h2database" % "h2" % "1.3.170",

    "org.jfree" % "jfreechart" % "1.0.14",
    "com.typesafe.slick" %% "slick" % "1.0.0",
    "org.mockito" % "mockito-all" % "1.9.0" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
