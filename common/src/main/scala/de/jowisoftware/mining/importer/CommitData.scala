package de.jowisoftware.mining.importer
import java.util.Date

object CommitData {
  def apply(id: String) = new CommitData(id)
}

class CommitData private(id: String) extends FieldList(CommitDataFields) {
  this(CommitDataFields.id) = id -> "(system)"
}