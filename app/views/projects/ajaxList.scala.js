@(projects: Seq[Project])

$("#project-list").html("@views.html.projects.list(projects)");