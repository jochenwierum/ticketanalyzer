package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes._
import de.jowisoftware.neo4j.content._
import helper._

object ChangedFile extends RelationshipCompanion[ChangedFile] {
  def apply = new ChangedFile

  val relationType = RelTypes.changedFile

  type sourceType = Commit
  type sinkType = File
}

class ChangedFile extends Relationship {
  val companion = ChangedFile

  val version = 1
  def updateFrom(version: Int) = {}

  val editType = stringProperty("editType")
}