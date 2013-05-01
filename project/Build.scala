import sbt._
import PlayKeys._
import Keys._

object ApplicationBuild extends Build {

  val appName = "walldee"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    "com.h2database" % "h2" % "1.3.170",

    "org.jfree" % "jfreechart" % "1.0.14",
    "com.typesafe.slick" %% "slick" % "1.0.0",
    "jaxen" % "jaxen" % "1.1.4" intransitive(),
    "org.jsoup" % "jsoup" % "1.7.2" intransitive(),
    "org.mockito" % "mockito-all" % "1.9.0" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    scalacOptions += "-feature",
    templatesTypes := {
      case "html" => ("play.api.templates.Html", "play.api.templates.HtmlFormat")
      case "txt" => ("play.api.templates.Txt", "play.api.templates.TxtFormat")
      case "xml" => ("play.api.templates.Xml", "play.api.templates.XmlFormat")
      case "js" => ("templates.Js", "templates.JsFormat")
    }
  )

}
