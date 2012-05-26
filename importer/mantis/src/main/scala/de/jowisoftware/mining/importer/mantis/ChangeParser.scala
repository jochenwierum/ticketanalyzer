package de.jowisoftware.mining.importer.mantis

import scala.xml.Node
import de.jowisoftware.mining.importer.TicketData.TicketField._
import java.util.Date
import grizzled.slf4j.Logging
import de.jowisoftware.mining.importer.TicketData
import de.jowisoftware.mining.importer.TicketRelationship

trait Change {
  val date: Date
  def update(ticket: TicketData)
  def downgrade(ticket: TicketData)
}

class SimpleChange[T](val date: Date, field: TicketField[T], oldValue: T, newValue: T, user: String) extends Change {
  def update(ticket: TicketData) = ticket(field) = newValue -> user
  def downgrade(ticket: TicketData) = ticket(field) = oldValue -> user
}

class SplitChange[T](val date: Date, field: TicketField[String], pos: Int, oldValue: String, newValue: String, user: String) extends Change {
  def update(ticket: TicketData) = ticket(field) = replaceSegment(pos, ticket(field), newValue) -> user
  def downgrade(ticket: TicketData) = ticket(field) = replaceSegment(pos, ticket(field), oldValue) -> user

  private def replaceSegment(field: Int, original: String, newSegment: String) = {
    val segs = original.split(":", -1)
    segs(field) = newSegment
    segs.mkString(":")
  }
}

class ArrayChange[T](val date: Date, field: TicketField[Seq[T]], oldValue: Option[T], newValue: Option[T], user: String) extends Change {
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

object ChangeParser {
  private val noteAddRegex = """Note Added: 0*(\d+)""".r
  private val tagAddedRegex = """Tag Attached: (.+)""".r
  private val noteDeletedRegex = """Note Deleted: 0*(\d+)""".r
  private val tagDetachedRegex = """Tag Detached: (.+)""".r

  private val sponsorRegex = """([^:]+): """.r
  private val relationshipRegex = """^(.*)\s+0*(\d+)$""".r

  private val viewState = """Note View State: .*""".r
  private val issueMonitorRegex = """Issue (?:Monitored|End Monitor):.*""".r
  private val noteViewStateRegex = """Note View State:.*""".r
}

class ChangeParser extends Logging {
  import ChangeParser._

  def parse(tr: Node, newTicket: TicketData): Option[Change] = {
    val cols = tr \ "td"

    val date = MantisImporter.fromSimpleDate(cols(0).text.trim)
    val user = cols(1).text.trim
    identifyChange(date, user, cols(2).text.trim, cols(3).text.trim, newTicket)
  }

  private def identifyChange(
    date: Date,
    user: String,
    field: String,
    change: String,
    ticket: TicketData): Option[Change] = {
    val (oldValue, newValue) = guessOldNew(change)

    implicit def change2SomeChange[T](c: Change) = Some(c)
    def wrapDefaultString[T](f: TicketField[String]) = new SimpleChange(date, f, oldValue, newValue, user)
    def wrapDefaultInt[T](f: TicketField[Int]) = new SimpleChange(date, f, oldValue.toInt, newValue.toInt, user)

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

  private def guessOldNew(change: String) = {
    val segs = change.split("""\s*=>\s*""", 2)

    if (segs.length > 1)
      (segs(0), segs(1))
    else
      ("", "")
  }

  private def findSponsor(change: String) = change match {
    case sponsorRegex(name) => name
    case _ => "unknown"
  }

  private def processRelationship(change: String) = change match {
    case relationshipRegex(relationString, ticketId) =>
      ValueUtils.relationshipStringToRelationshipType(relationString) match {
        case Some(ticketRel) => TicketRelationship(ticketId, ticketRel)
        case None => sys.error("Unparsable Relationship: "+relationString)
      }
    case _ => sys.error("Unparsable String: "+change)
  }
}