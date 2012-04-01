package de.jowisoftware.mining.model
import org.neo4j.graphdb.RelationshipType
import _root_.de.jowisoftware.neo4j._

object RelTypes {
  case class ScalaRelationshipType(val name: String) extends RelationshipType
  val contains = ScalaRelationshipType("contains")
  
  val inVersion = ScalaRelationshipType("in_version")
  val inComponent = ScalaRelationshipType("in_component")
  val hasType = ScalaRelationshipType("has_type")
  val inMilestone = ScalaRelationshipType("in_milestone")
  val hasStatus = ScalaRelationshipType("has_status")
  val fromPerson = ScalaRelationshipType("from_person")
  val owns = ScalaRelationshipType("owns")
  val reportedBy = ScalaRelationshipType("reported_by")
  val changedFile = ScalaRelationshipType("changed_file")
}



trait EmptyRelationship extends Relationship {
  val version = 0
  def updateFrom(version: Int) = { }
}



object IndexTicketRepository extends RelationshipCompanion[IndexTicketRepository] {
  def apply = new IndexTicketRepository
  
  val relationType = RelTypes.contains
  
  type sourceType = RootNode
  type sinkType = TicketRepository
}

class IndexTicketRepository extends EmptyRelationship {
  val companion = IndexTicketRepository
}



object Contains extends RelationshipCompanion[Contains] {
  def apply = new Contains
  
  val relationType = RelTypes.contains
  
  type sourceType = Node
  type sinkType = Node
}

class Contains extends EmptyRelationship {
  val companion = Contains
}



object InVersion extends RelationshipCompanion[InVersion] {
  def apply = new InVersion
  
  val relationType = RelTypes.inVersion
  
  type sourceType = Ticket
  type sinkType = Version
}

class InVersion extends EmptyRelationship {
  val companion = InVersion
}



object InComponent extends RelationshipCompanion[InComponent] {
  def apply = new InComponent
  
  val relationType = RelTypes.inComponent
  
  type sourceType = Ticket
  type sinkType = Component
}

class InComponent extends EmptyRelationship {
  val companion = InComponent
}



object HasType extends RelationshipCompanion[HasType] {
  def apply = new HasType
  
  val relationType = RelTypes.hasType
  
  type sourceType = Ticket
  type sinkType = Type
}

class HasType extends EmptyRelationship {
  val companion = HasType
}



object InMilestone extends RelationshipCompanion[InMilestone] {
  def apply = new InMilestone
  
  val relationType = RelTypes.inMilestone
  
  type sourceType = Ticket
  type sinkType = Milestone
}

class InMilestone extends EmptyRelationship {
  val companion = InMilestone
}



object HasStatus extends RelationshipCompanion[HasStatus] {
  def apply = new HasStatus
  
  val relationType = RelTypes.hasStatus
  
  type sourceType = Ticket
  type sinkType = Status
}

class HasStatus extends EmptyRelationship {
  val companion = HasStatus
}



object FromPerson extends RelationshipCompanion[FromPerson] {
  def apply = new FromPerson
  
  val relationType = RelTypes.fromPerson
  
  type sourceType = Node
  type sinkType = Person
}

class FromPerson extends EmptyRelationship {
  val companion = FromPerson
}



object Owns extends RelationshipCompanion[Owns] {
  def apply = new Owns
  
  val relationType = RelTypes.owns
  
  type sourceType = Node
  type sinkType = Person
}

class Owns extends EmptyRelationship {
  val companion = Owns
}



object ReportedBy extends RelationshipCompanion[ReportedBy] {
  def apply = new ReportedBy
  
  val relationType = RelTypes.reportedBy
  
  type sourceType = Node
  type sinkType = Person
}

class ReportedBy extends EmptyRelationship {
  val companion = ReportedBy
}



object ChangedFile extends RelationshipCompanion[ChangedFile] {
  def apply = new ChangedFile
  
  val relationType = RelTypes.changedFile
  
  type sourceType = Commit
  type sinkType = File
}

class ChangedFile extends Relationship {
  val companion = ReportedBy
  
  val version = 1
  def updateFrom(version: Int) = {}
  
  val editType = stringProperty("editType")
}