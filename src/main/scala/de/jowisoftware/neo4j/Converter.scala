package de.jowisoftware.neo4j

import org.neo4j.graphdb.{Node => NeoNode, Relationship => NeoRelationship}

object Converter {
  implicit def neoNode2Node[T <: Node](neoNode: NeoNode)(implicit manifest: Manifest[T]): T = {
    val node = manifest.erasure.newInstance().asInstanceOf[T]
    node initWith neoNode
    node
  }
  
  implicit def Node2NeoNode(node: Node): NeoNode = node.content
  
  /*
  implicit def NeoRelationship2Relationship[U <: Node, V <: Node, T <: Relationship[U, V]]
      (neoRelationship: NeoRelationship)(implicit manifest: Manifest[T]): T = {
    val relationship = manifest.erasure.newInstance().asInstanceOf[T]
    relationship initWith neoRelationship
    relationship
  }
  
  implicit def relationship2NeoRelationship(relationship: Relationship[_, _]) =
    relationship.getRelationship
    */
}