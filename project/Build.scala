import sbt._
import sbt.Keys._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName = "walldee"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    cache,
    ws,
    "com.h2database" % "h2" % "1.3.170",

    "org.jfree" % "jfreechart" % "1.0.14",
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "org.jsoup" % "jsoup" % "1.7.2" intransitive(),
    "org.mockito" % "mockito-all" % "1.9.0" % "test"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    scalacOptions += "-feature",

    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    LessKeys.compress := true, // for minified *.min.css files
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",

    pipelineStages in Assets := Seq(uglify)
  )

}
