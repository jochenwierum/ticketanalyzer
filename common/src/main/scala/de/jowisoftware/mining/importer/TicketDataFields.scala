package de.jowisoftware.mining.importer

import java.util.Date

object TicketDataFields extends FieldListData {
  val editor = field[Option[String]]("user", None)

  val id = field("id", 0)
  val summary = field("summary", "")
  val description = field("description", "")

  val creationDate = field("creationDate", new Date)
  val updateDate = field("updateDate", new Date)
  val startDate = field("startDate", new Date)
  val dueDate = field("dueDate", new Date)
  val status = field("status", "")
  val progress = field("progress", 0)

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
  val spentTime = field("spentTime", 0.0f)

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