@(teams: Seq[Team])

$("#team-list").html("@views.html.teams.list(teams)");