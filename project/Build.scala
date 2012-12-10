import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "walldee"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.h2database" % "h2" % "1.3.167",

    "org.jfree" % "jfreechart" % "1.0.14",
    "org.scalaquery" %% "scalaquery" % "0.10.0-M1",
    "org.mockito" % "mockito-all" % "1.9.0" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
