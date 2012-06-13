package de.jowisoftware.mining.importer
import java.util.Date

object CommitData extends FieldListCompanion {
  object commitFields extends FieldListData {
    val id = field("id", "")
    val author = field("author", "")
    val message = field("message", "")
    val date = field("date", new Date())
    val files = field("files", Map[String, String]())
    val parents = field("parents", Seq[String]())
  }

  def apply(id: String) = new CommitData(id)
}

class CommitData private(id: String) extends FieldList {
  val fieldListData = CommitData.commitFields

  this(fieldListData.id) = id -> "(system)"
}

/*
case class CommitData(id: String,
  author: String = "", message: String = "", date: Date = new Date(),
  files: Map[String, String] = Map(), parents: Seq[String] = Seq())
*/