package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models._
import play.api.data.validation.{ValidationError, Invalid, Valid, Constraint}
import play.api.libs.ws.WS
import com.ning.http.client.Realm.AuthScheme
import scala.concurrent.Future
import org.jsoup.Jsoup
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.net.{URI, URL}


object Wizards extends Controller {

  case class Wizard(
    displayName: String,
    projectName: String,
    ciServer: String,
    url: String,
    username: Option[String],
    password: Option[String]
  )

  val teamCityCheckConstraint: Constraint[String] = Constraint("constraints.onlyTeamCity")({
    case "Teamcity" => Valid
    case _ => Invalid(Seq(ValidationError("only team city is supported")))
  })

  val wizardForm = Form(
    mapping(
      "displayName" -> nonEmptyText(maxLength = 255),
      "projectName" -> nonEmptyText(maxLength = 255),
      "ciServer" -> nonEmptyText().verifying(teamCityCheckConstraint),
      "url" -> nonEmptyText(),
      "username" -> optional(text),
      "password" -> optional(text)
    )(Wizard.apply)(Wizard.unapply)
  )

  def index() = Action {
    showForm(wizardForm)
  }

  private def showForm(form: Form[Wizard]) = {
    Ok(views.html.wizards.index(form))
  }

  def create() = Action.async { implicit request =>
    wizardForm.bindFromRequest().fold (
      hasErrors = formWithErrors => Future.successful(showForm(formWithErrors)),
      success = wizard => importTeamCityProject(wizard).map { id =>
        Redirect(routes.Displays.showWall(id))
      }
    )
  }

  private def importTeamCityProject(wizard: Wizard): Future[Long] = {
    import collection.JavaConversions._

    val wsRequest = (
      for (username <- wizard.username; password <- wizard.password)
        yield WS.url(wizard.url).withAuth(username, password, AuthScheme.BASIC)
      ).getOrElse(WS.url(wizard.url))

    val projectURL = new URL(wizard.url)
    val baseURL = new URI(projectURL.getProtocol, null, projectURL.getHost, projectURL.getPort, null, null, null).toString

    wsRequest.get().map {
      case response if response.status != 200 => throw new Exception(s"cannot parse response [status=${response.status}, url=${wizard.url}] from TeamCity: ${response.body}")
      case response => {
        val document = Jsoup.parse(response.body, wizard.url)
        val steps = document.select(".buildTypeName").iterator().toList
        val projectSteps = for {
          step <- steps
          stepName = step.text()
          relativeURI = step.attr("href")
          stepURL = baseURL + relativeURI
        } yield stepName -> stepURL

        // no DB transaction. parsed data can always be used
        val projectId = findOrCreateProject(wizard.projectName)
        StatusMonitor.finaAllForProject(projectId).foreach(_.delete)

        for (projectStep <- projectSteps) {
          val statusMonitor = StatusMonitor(
            projectId = projectId,
            name = projectStep._1,
            typeNum = StatusMonitorTypes.Teamcity.id,
            url = projectStep._2,
            username = wizard.username,
            password = wizard.password,
            active = true,
            keepHistory = 10,
            updatePeriod = 60
          )
          statusMonitor.insert
        }

        val teamId = findOrCreateTeam(wizard.displayName)
        val displayId = findOrCreateDisplay(projectId, teamId, wizard.displayName)

        val existingDisplayItems = DisplayItem.findAllForDisplay(displayId)
        val displayAlreadyExistForProject = existingDisplayItems.exists(_.projectId.exists(_ == projectId))

        if (!displayAlreadyExistForProject) {
          // add display for project
          val maxX = existingDisplayItems.foldLeft(0){ case (max, d) => Math.max(d.posx + d.width, max) }

          val title = DisplayItem(
            displayId = displayId,
            posx = maxX + 20,
            posy = 0,
            width = 400,
            height = 25,
            widgetNum = DisplayWidgets.Heading.id,
            projectId = Some(projectId),
            teamId = Some(teamId),
            appearsInFeed = true,
            hidden = false,
            widgetConfigJson = s"""{"title": "${wizard.projectName}"}"""
          )
          title.insert

          val displayItem = DisplayItem(
            displayId = displayId,
            posx = maxX + 20,
            posy = 30,
            width = 400,
            height = 30 + projectSteps.length * 25,
            widgetNum = DisplayWidgets.BuildStatus.id,
            projectId = Some(projectId),
            teamId = Some(teamId),
            appearsInFeed = true,
            hidden = false,
            widgetConfigJson = "{}"
          )
          displayItem.insert

        }
        displayId
      }
    }
  }

  private def findOrCreateProject(projectName: String): Long = {
    (for {
      project <- Project.findByName(projectName)
      id <- project.id
    } yield id).getOrElse {
      val project = new Project(name = projectName)
      val projectId = project.insert
      projectId
    }
  }

  private def findOrCreateTeam(name: String): Long = {
    (for {
      team <- Team.findFirstByName(name)
      id <- team.id
    } yield id).getOrElse {
      val team = Team(name = name)
      val teamId = team.insert
      teamId
    }
  }

  private def findOrCreateDisplay(projectId: Long, teamId: Long, defaultName: String): Long = {
    (for {
      display <- Display.findFirstForTeam(teamId = teamId)
      id <- display.id
    } yield id).getOrElse {
      val display = Display(
        name = defaultName,
        projectId = Some(projectId),
        teamId = Some(teamId),
        styleNum = DisplayStyles.Normal.id,
        refreshTime = 5,
        useLongPolling = true,
        relativeLayout = false,
        animationConfigJson = "{}"
      )
      val displayId = display.insert
      displayId
    }
  }

}
