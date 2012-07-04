package de.jowisoftware.mining.importer.redmine

import scala.xml.Node
import java.util.Date
import de.jowisoftware.util.XMLUtils._
import de.jowisoftware.mining.importer._
import de.jowisoftware.mining.importer.TicketDataFields._
import grizzled.slf4j.Logging
import org.neo4j.graphalgo.EstimateEvaluator

class ChangeParser(resolver: CachedResolver, private var project: String) extends Logging {
  def createChangesList(hasComment: Boolean, node: Node) = {
    val id = (node \ "@id" intText)
    val date = RedmineImporter.timeParser.parseDateTime(node \ "created_on" text).toDate
    val user = resolver.getUser(node \ "user" \ "@id" intText)

    val changes = (node \ "details" \ "detail" map {
      node => createDetail(date, node, user)
    }).flatten

    if (hasComment)
      createCommentChange(user, id, date) +: changes
    else
      changes
  }

  def createDetail(date: Date, node: Node, user: String): Option[Change] = {
    implicit def wrapChange(change: Change) = Some(change)

    val oldValue = (node \ "old_value" text)
    val newValue = (node \ "new_value" text)

    def stringChange(field: TicketDataFields.FieldDescription[String]) =
      new SimpleChange(date, field, oldValue, newValue, user)

    def intChange(field: TicketDataFields.FieldDescription[Int]) =
      new SimpleChange(date, field, oldValue.toInt, newValue.toInt, user)

    def floatChange(field: TicketDataFields.FieldDescription[Float]) =
      new SimpleChange(date, field, oldValue.toFloat, newValue.toFloat, user)

    def lookupChange(field: TicketDataFields.FieldDescription[String], lookupMethod: Int => String) =
      if (oldValue.isEmpty)
        new SimpleChange(date, field, "", lookupMethod(newValue.toInt), user)
      else if (newValue.isEmpty)
        new SimpleChange(date, field, lookupMethod(oldValue.toInt), "", user)
      else
        new SimpleChange(date, field, lookupMethod(oldValue.toInt), lookupMethod(newValue.toInt), user)

    (node \ "@name" text) match {
      case "tracker_id" => lookupChange(ticketType, resolver.getTracker(project))
      case "subject" => stringChange(summary)
      case "description" => stringChange(description)
      case "status_id" => lookupChange(status, resolver.getStatus)
      case "done_ratio" => intChange(progress)
      case "estimated_hours" => floatChange(eta)
      case "assigned_to_id" => lookupChange(owner, resolver.getUser)
      case "category_id" => lookupChange(component, resolver.getCategory)
      case "fixed_version_id" => lookupChange(fixedInVersion, resolver.getVersion)

      case "priority_id" => None

      case "project_id" =>
        trace("Switching project lookups to subproject "+oldValue)
        project = oldValue
        None

      case unknown =>
        warn("Ignoring unknown field: "+unknown)
        None
    }
  }

  private def createCommentChange(user: String, id: Int, date: java.util.Date) = {
    new ArrayChange(date, comments, None, Some(id), user)
  }
}