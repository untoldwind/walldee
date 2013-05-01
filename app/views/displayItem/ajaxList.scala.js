@(display:Display, displayItems:Seq[DisplayItem])

$("#displayItem-list").html("@views.html.displayItem.list(display,displayItems)");