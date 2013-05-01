@(displays: Seq[Display])

$("#display-list").html("@views.html.display.list(displays)");