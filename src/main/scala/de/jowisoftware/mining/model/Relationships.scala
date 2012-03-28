package de.jowisoftware.mining.model
import org.neo4j.graphdb.RelationshipType
import _root_.de.jowisoftware.neo4j._

object RelTypes {
  case class ScalaRelationshipType(val name: String) extends RelationshipType
  val references = ScalaRelationshipType("references")
  val follows = ScalaRelationshipType("follows")
}

object Follows extends RelationshipCompanion[Follows] {
  def apply = new Follows
  
  val relationType = RelTypes.follows
  
  type leftType = Commit
  type rightType = Commit 
}

class Follows extends Relationship {
  val companion = Follows
  
  val version = 1
  def updateFrom(version: Int) = { }
}

object Reference extends RelationshipCompanion[Reference] {
  def apply = new Reference
  
  val relationType = RelTypes.follows
  
  type leftType = Node
  type rightType = Node 
}

class Reference extends Relationship {
  val companion = Follows
  
  val version = 1
  def updateFrom(version: Int) = { }
}