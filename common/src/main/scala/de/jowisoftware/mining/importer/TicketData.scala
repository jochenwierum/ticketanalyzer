package de.jowisoftware.mining.importer

import java.util.Date
import scala.collection.SortedMap

object TicketData extends FieldListCompanion {
  object ticketFields extends FieldListData {
    val id = field("id", 0)
    val summary = field("summary", "")
    val description = field("description", "")

    val creationDate = field("creationDate", new Date)
    val updateDate = field("updateDate", new Date)
    val status = field("status", "")

    val version = field("version", "")
    val fixedInVersion = field("fixedInVersion", "")
    val targetVersion = field("targetVersion", "")
    val milestone = field("milestone", "")

    val tags = field("tags", Seq[String]())
    val component = field("component", "")

    val reporter = field("reporter", "")
    val owner = field("owner", "")
    val votes = field("votes", 0)
    val eta = field("eta", 0)
    val sponsors = field("sponsors", Seq[String]())

    val ticketType = field("ticketType", "")
    val resolution = field("resolution", "")
    val priority = field("priority", "")
    val severity = field("severity", "")
    val reproducability = field("reproducability", "")

    val relationships = field("relationships", Seq[TicketRelationship]())

    val environment = field("environment", "")
    val build = field("build", "")

    val comments = field("comments", Seq[Int]())
  }

  def apply(id: Int) = {
    val result = new TicketData()
    result(ticketFields.id) = (id, "(system)")
    result
  }
}

class TicketData(reference: TicketData) extends FieldList {
  import TicketData._
  val fieldListData = ticketFields

  def this() = this(null)

  values += ticketFields.creationDate -> (new Date(), "")
  values += ticketFields.updateDate -> (new Date(), "")

  if (reference != null)
    reference.values.foreach { case (k, v) => values += k -> v }
}