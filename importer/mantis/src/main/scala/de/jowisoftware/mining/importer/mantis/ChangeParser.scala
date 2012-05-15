package de.jowisoftware.mining.importer.mantis

import scala.xml.Node
import de.jowisoftware.mining.importer.TicketData.TicketField._
import java.util.Date
import grizzled.slf4j.Logging
import de.jowisoftware.mining.importer.TicketData

trait Change {
  val date: Date
  def update(ticket: TicketData)
  def downgrade(ticket: TicketData)
}

class SimpleChange[T](val date: Date, field: TicketField[T], oldValue: T, newValue: T, user: String) extends Change {
  def update(ticket: TicketData) = ticket(field) = newValue -> user
  def downgrade(ticket: TicketData) = ticket(field) = oldValue -> user
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
  private val noteAddRegex = """"Note Added: 0*(\d+)""".r
  private val tagAddedRegex = """Tag Attached: (.+)""".r
  private val viewState = """Note View State: .*""".r
  private val sponsorRegex = """([^:]+): """.r
  private val noteDeletedRegex = """Note Deleted: (\d+)""".r
  private val tagDetachedRegex = """Tag Detached: (.+)""".r

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
      case "New Issue" => None
      case "Issue cloned" => None
      case "Additional Information Updated" => None
      case "Description Updated" => None
      case `issueMonitorRegex` => None
      case `noteViewStateRegex` => None
      case "Project" => None

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

      case "ETA" => new SimpleChange(date, eta, ValueUtils.etaStringToInt(oldValue), ValueUtils.etaStringToInt(newValue), user)

      case "Platform" => new SimpleChange(date, environment, updateOs(0, ticket(environment), oldValue), ticket(environment), user)
      case "OS" => new SimpleChange(date, environment, updateOs(1, ticket(environment), oldValue), ticket(environment), user)
      case "OS Version" => new SimpleChange(date, environment, updateOs(2, ticket(environment), oldValue), ticket(environment), user)

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

  private def updateOs(field: Int, oldValue: String, newValue: String) = {
    val segs = oldValue.split(":", -1)
    segs(field) = newValue
    segs.mkString(":")
  }

  private def processRelationship(change: String) = {
    val segs = change.split("""\s*of\s*""")
    segs(1)+":"+segs(0)
  }
}