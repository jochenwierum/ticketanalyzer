package de.jowisoftware.mining.model.relationships

import de.jowisoftware.mining.model.nodes.FileRepository
import de.jowisoftware.neo4j.content.{RelationshipCompanion, Node}
import helper.{RelTypes, EmptyRelationship}

object ContainsFiles extends RelationshipCompanion[ContainsFiles] {
  def apply = new ContainsFiles

  val relationType = RelTypes.containsFiles

  type sourceType = Node
  type sinkType = FileRepository
}

class ContainsFiles extends EmptyRelationship {
  val companion = ContainsFiles
}