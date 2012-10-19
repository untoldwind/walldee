package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object WallDeeSchema extends Schema {
  val sprints = table[Sprint]

  val stories = table[Story]

  val sprintToStories = oneToManyRelation(sprints, stories).via((sprint, story) => sprint.id === story.sprintId)

  val dayCounts = table[DayCount]

  val sprintToDayCounts =
    oneToManyRelation(sprints, dayCounts).via((sprint, dayCount) => sprint.id === dayCount.sprintId)

  val displays = table[Display]

  val sprintToDisplays = oneToManyRelation(sprints, displays).via((sprint, display) => sprint.id === display.sprintId)

  val displayItems = table[DisplayItem]

  val displayToDisplayItems =
    oneToManyRelation(displays, displayItems).via((display, displayItem) => display.id === displayItem.displayId)
}
