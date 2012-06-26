package de.jowisoftware.mining.importer.mantis

import scala.xml.Node
import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer.TicketDataFields
import java.util.Date
import grizzled.slf4j.Logging
import de.jowisoftware.mining.importer.TicketData
import de.jowisoftware.mining.importer.TicketRelationship

trait Change {
  val date: Date
  def update(ticket: TicketData)
  def downgrade(ticket: TicketData)
}

class SimpleChange[T](val date: Date, field: FieldDescription[T], oldValue: T, newValue: T, user: String) extends Change {
  def update(ticket: TicketData) = ticket(field) = newValue -> user
  def downgrade(ticket: TicketData) = ticket(field) = oldValue -> user
}

class ArrayChange[T](val date: Date, field: FieldDescription[Seq[T]], oldValue: Option[T], newValue: Option[T], user: String) extends Change {
  private def remove(ticket: TicketData, value: T) = ticket(field) = ticket(field).filterNot(_ == value) -> user
  private def add(ticket: TicketData, value: T) = ticket(field) = (ticket(field) :+ value) -> user

  private def replace(ticket: TicketData, oldValue: T, newValue: T) =
    ticket(field) = ticket(field).map { value => if (value == oldValue) newValue else value } -> user

  def update(ticket: TicketData) =
    if (newValue == None)
      remove(ticket, oldValue.get)
    else if (oldValue == None)
      add(ticket, newValue.get)
    else
      replace(ticket, oldValue.get, newValue.get)

  def downgrade(ticket: TicketData) =
    if (oldValue == None)
      remove(ticket, newValue.get)
    else if (newValue == None)
      add(ticket, oldValue.get)
    else
      replace(ticket, newValue.get, oldValue.get)
}

object ChangeParser extends Logging {

  def wrapChange(
    date: Date,
    user: String,
    field: String,
    change: String,
    ticket: TicketData): Option[Change] = {
    val (oldValue, newValue) = guessOldNew(change)

    implicit def change2SomeChange[T](c: Change) = Some(c)
    def wrapDefaultString[T](f: FieldDescription[String]) = new SimpleChange(date, f, oldValue, newValue, user)
    def wrapDefaultInt[T](f: FieldDescription[Int]) = new SimpleChange(date, f, oldValue.toInt, newValue.toInt, user)

    field match {
      case "New Issue" | "Issue cloned" | "Additional Information Updated" | "Description Updated" | "Project" | "Sponsorship Updated" | "Sponsorship Paid" => None
      case issueMonitorRegex() | noteViewStateRegex() => None

      case "Status" => wrapDefaultString(status)
      case "Assigned To" => wrapDefaultString(owner)
      case "Reproducibility" => wrapDefaultString(reproducability)
      case "Category" => wrapDefaultString(component)
      case "version" => wrapDefaultString(version)
      case "Build" => wrapDefaultString(build)
      case "Fixed in Version" => wrapDefaultString(fixedInVersion)
      case "Target Version" => wrapDefaultString(targetVersion)
      case "Sponsorship Total" => wrapDefaultInt(votes)
      case "Summary" => wrapDefaultString(summary)
      case "Resolution" => wrapDefaultString(resolution)

      case "ETA" => new SimpleChange(date, eta, ValueUtils.etaStringToInt(oldValue), ValueUtils.etaStringToInt(newValue), user)

      case "Platform" => new SplitChange(date, environment, 0, oldValue, newValue, user)
      case "OS" => new SplitChange(date, environment, 1, oldValue, newValue, user)
      case "OS Version" => new SplitChange(date, environment, 2, oldValue, newValue, user)

      case "Relationship added" => new ArrayChange(date, relationships, None, Some(processRelationship(change)), user)
      case "Sponsorship Added" => new ArrayChange(date, sponsors, None, Some(findSponsor(change)), user)
      case noteAddRegex(id) => new ArrayChange(date, comments, None, Some(id.toInt), user)
      case noteDeletedRegex(id) => new ArrayChange(date, comments, Some(id.toInt), None, user)
      case tagAddedRegex(tag) => new ArrayChange(date, tags, None, Some(tag), user)
      case tagDetachedRegex(tag) => new ArrayChange(date, tags, Some(tag), None, user)

      case unknown =>
        warn("Unknown field: "+unknown+" (\""+oldValue+"\" -> \""+newValue+"\")")
        None
    }
  }

  private def processRelationship(change: String) = change match {
    case relationshipRegex(relationString, ticketId) =>
      ValueUtils.relationshipStringToRelationshipType(relationString) match {
        case Some(ticketRel) => TicketRelationship(ticketId.toInt, ticketRel)
        case None => sys.error("Unparsable Relationship: "+relationString)
      }
    case _ => sys.error("Unparsable String: "+change)
  }
}