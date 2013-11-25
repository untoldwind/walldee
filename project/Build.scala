import sbt._
import sbt.Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "walldee"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    cache,
    "com.h2database" % "h2" % "1.3.170",

    "org.jfree" % "jfreechart" % "1.0.14",
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "org.jsoup" % "jsoup" % "1.7.2" intransitive(),
    "org.webjars" % "angularjs" % "1.2.1",
    "org.webjars" % "requirejs" % "2.1.1",
    "org.webjars" %% "webjars-play" % "2.2.1",
    "org.webjars" % "bootstrap" % "3.0.2",
    "org.webjars" % "bootstrap-glyphicons" % "bdd2cbfba0",
    "org.webjars" % "ng-grid" % "2.0.7",
    "org.webjars" % "jquery-ui" % "1.10.3",
    "org.webjars" % "jquery" % "1.10.2-1",
    "org.webjars" % "angular-ui" % "0.4.0-1",
    "org.webjars" % "angular-ui-bootstrap" % "0.6.0-1",
    "org.webjars" % "angular-ui-utils" % "47ff7ef35c",
    "org.mockito" % "mockito-all" % "1.9.0" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings {
    // Add your own project settings here
    scalacOptions += "-feature"
  }
}
