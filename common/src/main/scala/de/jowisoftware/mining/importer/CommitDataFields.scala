package de.jowisoftware.mining.importer

import java.util.Date

object CommitDataFields extends FieldListData {
  val id = field("id", "")
  val author = field("author", "")
  val message = field("message", "")
  val date = field("date", new Date())
  val files = field("files", Map[String, String]())
  val parents = field("parents", Seq[String]())
}