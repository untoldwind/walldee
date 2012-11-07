package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object WallDeeSchema extends Schema {
  val sprints = table[Sprint]

  val stories = table[Story]

  val sprintToStories = oneToManyRelation(sprints, stories).via((sprint, story) => sprint.id === story.sprintId)
}
