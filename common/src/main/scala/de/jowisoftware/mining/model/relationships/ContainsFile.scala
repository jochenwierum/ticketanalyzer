package de.jowisoftware.mining.model.relationships

import de.jowisoftware.neo4j.content.{ RelationshipCompanion, Node }
import helper.{ RelTypes, EmptyRelationship }
import de.jowisoftware.mining.model.nodes.CommitRepository
import de.jowisoftware.mining.model.nodes.File

object ContainsFile extends RelationshipCompanion[ContainsFile] {
  def apply = new ContainsFile

  val relationType = RelTypes.containsFiles

  type sourceType = CommitRepository
  type sinkType = File
}

class ContainsFile extends EmptyRelationship {
  val companion = ContainsFile
}
