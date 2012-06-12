package de.jowisoftware.mining.importer

import java.util.Date

object TicketCommentData extends FieldListCompanion {
  object ticketCommentFields extends FieldListData {
    val id = field("id", 0)
    val text = field("text", "")
    val author = field("author", "")
    val created = field("created", new Date())
    val modified = field("modified", new Date())
  }
}

class TicketCommentData extends FieldList {
  import TicketCommentData._
  protected val fieldListData = ticketCommentFields
}
