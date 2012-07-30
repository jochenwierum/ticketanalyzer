package de.jowisoftware.mining.importer.trac

import java.util.Date

import de.jowisoftware.mining.importer._
import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer.TicketRelationship
import de.jowisoftware.mining.importer.TicketRelationship.RelationshipType
import grizzled.slf4j.Logging

object ChangeParser {
  private val commentParentRegex = """(?:(\d+)\.)?(\d+)""".r
  private val ignoreComment = """_comment\d+""".r
}

class ChangeParser extends Logging {
  private var currentBlockReferences: Set[Int] = Set()

  def createComment(
    date: Date,
    user: String,
    field: String,
    oldValue: String,
    newValue: String) =

    oldValue match {
      case ChangeParser.commentParentRegex(parentId, id) =>
        val comment = new TicketCommentData
        comment(TicketCommentDataFields.id) = id.toInt
        comment(TicketCommentDataFields.text) = newValue
        comment(TicketCommentDataFields.author) = user
        comment(TicketCommentDataFields.created) = date
        comment(TicketCommentDataFields.modified) = date
        if (parentId != null) {
          comment(TicketCommentDataFields.parent) = Some(parentId.toInt)
        } else {
          comment(TicketCommentDataFields.parent) = None
        }
        comment
      case _ =>
        sys.error("Unexpected ticket comment id: "+oldValue)
    }

  def wrapChange(
    date: Date,
    user: String,
    field: String,
    oldValue: String,
    newValue: String,
    ticket: TicketData): Option[Change] = {

    implicit def change2SomeChange[T](c: Change) = Some(c)
    def wrap[T](f: FieldDescription[String]) = new SimpleChange(date, f, oldValue, newValue, user)

    field match {
      case "component" => wrap(component)
      case "description" => wrap(description)
      case "keywords" => new SimpleChange(date, tags, oldValue.split(' ').toSeq.sorted, newValue.split(' ').toSeq.sorted, user)
      case "milestone" => wrap(milestone)
      case "owner" => wrap(owner)
      case "priority" => wrap(priority)
      case "reporter" => wrap(reporter)
      case "status" => wrap(status)
      case "summary" => wrap(summary)
      case "type" => wrap(ticketType)
      case "version" => wrap(version)
      case "resolution" => wrap(resolution)
      case "comment" => new ArrayChange(date, comments, None, Some(newValue.toInt), user)
      case "blocking" if newValue.nonEmpty =>
        val allValues = newValue.split("""\s*,\s*""").map(_.toInt).toSet
        val newValues = (allValues -- currentBlockReferences).map(
          ticketId => TicketRelationship(ticketId, RelationshipType.blocks))
        val removedValues = (currentBlockReferences -- allValues).map(
          ticketId => TicketRelationship(ticketId, RelationshipType.blocks))
        currentBlockReferences = allValues
        new SetChange(date, relationships, removedValues, newValues, user)

      case "blocking" =>
        val oldReferences = currentBlockReferences.map(
          ticketId => TicketRelationship(ticketId, RelationshipType.blocks))
        currentBlockReferences = Set()
        new SetChange(date, relationships, oldReferences, Set.empty, user)
      // these are just ignored (the other ticket will already add a "blocking" dependency)
      case "blockedby" => None

      case "cc" => new SimpleChange(date, sponsors,
        oldValue.split("""\s*,\s*""").toSeq.sorted, newValue.split("""\s*,\s*""").toSeq.sorted, user)

      case ChangeParser.ignoreComment() => None

      case unknown =>
        warn("Unknown field: "+unknown+" (\""+oldValue+"\" -> \""+newValue+"\")")
        None
    }
  }
}