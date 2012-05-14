package de.jowisoftware.mining.importer.mantis

import scala.xml.Node
import de.jowisoftware.mining.importer.TicketData.TicketField._
import java.util.Date
import grizzled.slf4j.Logging
import de.jowisoftware.mining.importer.TicketData

case class Change[T](date: Date, field: TicketField[T], oldValue: String, newValue: String, user: String)

object ChangeParser {
  private val NoteAddRegex = """"Note Added: (\d+)""".r
  private val TagAddedRegex = """Tag Added: (.+)""".r
}

class ChangeParser extends Logging {
  import ChangeParser._

  def parse(tr: Node, newTicket: TicketData): Option[Change[_]] = {
    val cols = tr \ "td"

    val date = MantisImporter.fromSimpleDate(cols(0).text.trim)
    val user = cols(1).text.trim
    identifyChange(date, user, cols(2).text, cols(3).text, newTicket)
  }

  private def identifyChange(
    date: Date,
    user: String,
    field: String,
    change: String,
    ticket: TicketData): Option[Change[_]] = {
    val (oldValue, newValue) = guessOldNew(change)

    implicit def change2SomeChange[T](c: Change[T]) = Some(c)
    def wrapDefault[T](f: TicketField[T]) = Change(date, f, oldValue, newValue, user)

    field match {
      case "New Issue" => None
      case "Issue cloned" => None

      case "Status" => wrapDefault(status)
      case "Assigned To" => wrapDefault(owner)
      case "Reproducibility" => wrapDefault(reproducability)
      case "Category" => wrapDefault(component)
      case "version" => wrapDefault(version)
      case "Relationship added" => Change(date, comments, "", processRelationship(change), user)
      case "OS" => Change(date, environment, updateOs(1, ticket(environment), oldValue), ticket(environment), user)

      case NoteAddRegex(id) => Change(date, comments, "", id, user)
      case TagAddedRegex(tag) => Change(date, tags, "", tag, user)

      case unknown =>
        warn("Unknown field: "+unknown)
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

  private def updateOs(field: Int, oldValue: String, newValue: String) = {
    val segs = oldValue.split(":")
    segs(field) = newValue
    segs.mkString(":")
  }

  private def processRelationship(change: String) = {
    val segs = change.split("""\s*of\s*""")
    segs(1)+":"+segs(0)
  }
}