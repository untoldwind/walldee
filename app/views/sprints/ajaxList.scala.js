@(sprints: Seq[Sprint])

$("#sprint-list").html("@views.html.sprints.list(sprints)");