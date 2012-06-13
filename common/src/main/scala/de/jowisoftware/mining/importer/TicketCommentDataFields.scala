package de.jowisoftware.mining.importer

import java.util.Date

object TicketCommentDataFields extends FieldListData {
  val id = field("id", 0)
  val text = field("text", "")
  val author = field("author", "")
  val created = field("created", new Date())
  val modified = field("modified", new Date())
}