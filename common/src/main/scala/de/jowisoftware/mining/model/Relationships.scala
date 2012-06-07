package de.jowisoftware.mining.model
import org.neo4j.graphdb.RelationshipType
import _root_.de.jowisoftware.neo4j.content._

object RelTypes {
  case class ScalaRelationshipType(val name: String) extends RelationshipType

  val contains = ScalaRelationshipType("contains")

  val inVersion = ScalaRelationshipType("in_version")
  val fixedInVersion = ScalaRelationshipType("fixed_in_version")
  val targets = ScalaRelationshipType("targets")

  val inComponent = ScalaRelationshipType("in_component")
  val hasType = ScalaRelationshipType("has_type")
  val inMilestone = ScalaRelationshipType("in_milestone")
  val hasStatus = ScalaRelationshipType("has_status")
  val fromPerson = ScalaRelationshipType("from_person")
  val changedFile = ScalaRelationshipType("changed_file")
  val changedTicket = ScalaRelationshipType("changed_ticket")
  val hasResolution = ScalaRelationshipType("has_resolution")
  val hasSeverity = ScalaRelationshipType("has_severity")
  val hasReproducability = ScalaRelationshipType("has_reproducability")
  val hasPriority = ScalaRelationshipType("has_priority")

  val owns = ScalaRelationshipType("owns")
  val wrotes = ScalaRelationshipType("wrote")
  val reportedBy = ScalaRelationshipType("reported_by")
  val sponsoredBy = ScalaRelationshipType("sponsored_by")

  val hasTag = ScalaRelationshipType("has_tag")
  val hasComment = ScalaRelationshipType("has_comment")

  val updates = ScalaRelationshipType("updates")
  val references = ScalaRelationshipType("references")
}

trait EmptyRelationship extends Relationship {
  val version = 0
  def updateFrom(version: Int) = {}
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

object Targets extends RelationshipCompanion[Targets] {
  def apply = new Targets

  val relationType = RelTypes.targets

  type sourceType = Ticket
  type sinkType = Version
}

class Targets extends EmptyRelationship {
  val companion = Targets
}

object FixedInVersion extends RelationshipCompanion[FixedInVersion] {
  def apply = new FixedInVersion

  val relationType = RelTypes.fixedInVersion

  type sourceType = Ticket
  type sinkType = Version
}

class FixedInVersion extends EmptyRelationship {
  val companion = FixedInVersion
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

  type sourceType = Ticket
  type sinkType = Person
}

class Owns extends EmptyRelationship {
  val companion = Owns
}

object Wrotes extends RelationshipCompanion[Wrotes] {
  def apply = new Wrotes

  val relationType = RelTypes.wrotes

  type sourceType = TicketComment
  type sinkType = Person
}

class Wrotes extends EmptyRelationship {
  val companion = Wrotes
}

object ReportedBy extends RelationshipCompanion[ReportedBy] {
  def apply = new ReportedBy

  val relationType = RelTypes.reportedBy

  type sourceType = Ticket
  type sinkType = Person
}

class ReportedBy extends EmptyRelationship {
  val companion = ReportedBy
}

object SponsoredBy extends RelationshipCompanion[SponsoredBy] {
  def apply = new SponsoredBy

  val relationType = RelTypes.sponsoredBy

  type sourceType = Ticket
  type sinkType = Person
}

class SponsoredBy extends EmptyRelationship {
  val companion = SponsoredBy
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

object ChangedTicket extends RelationshipCompanion[ChangedTicket] {
  def apply = new ChangedTicket

  val relationType = RelTypes.changedTicket

  type sourceType = Person
  type sinkType = Ticket
}

class ChangedTicket extends Relationship {
  val companion = ChangedTicket

  val version = 1
  def updateFrom(version: Int) = {}
}

object HasTag extends RelationshipCompanion[HasTag] {
  def apply = new HasTag

  val relationType = RelTypes.hasTag

  type sourceType = Ticket
  type sinkType = Tag
}

class HasTag extends EmptyRelationship {
  val companion = HasTag
}

object HasPriority extends RelationshipCompanion[HasPriority] {
  def apply = new HasPriority

  val relationType = RelTypes.hasPriority

  type sourceType = Ticket
  type sinkType = Priority
}

class HasPriority extends EmptyRelationship {
  val companion = HasPriority
}

object HasSeverity extends RelationshipCompanion[HasSeverity] {
  def apply = new HasSeverity

  val relationType = RelTypes.hasSeverity

  type sourceType = Ticket
  type sinkType = Severity
}

class HasSeverity extends EmptyRelationship {
  val companion = HasSeverity
}

object HasReproducability extends RelationshipCompanion[HasReproducability] {
  def apply = new HasReproducability

  val relationType = RelTypes.hasReproducability

  type sourceType = Ticket
  type sinkType = Reproducability
}

class HasReproducability extends EmptyRelationship {
  val companion = HasReproducability
}

object HasResolution extends RelationshipCompanion[HasResolution] {
  def apply = new HasResolution

  val relationType = RelTypes.hasResolution

  type sourceType = Ticket
  type sinkType = Resolution
}

class HasResolution extends EmptyRelationship {
  val companion = HasResolution
}

object HasComment extends RelationshipCompanion[HasComment] {
  def apply = new HasComment

  val relationType = RelTypes.hasComment

  type sourceType = Ticket
  type sinkType = TicketComment
}

class HasComment extends EmptyRelationship {
  val companion = HasComment
}

object Updates extends RelationshipCompanion[Updates] {
  def apply = new Updates

  val relationType = RelTypes.updates

  type sourceType = Ticket
  type sinkType = Ticket
}

class Updates extends EmptyRelationship {
  val companion = Updates
}

object References extends RelationshipCompanion[References] {
  def apply = new References

  val relationType = RelTypes.references

  type sourceType = Ticket
  type sinkType = Ticket
}

class References extends Relationship {
  val companion = References

  val version = 1
  def updateFrom(oldVersion: Int) = {}

  lazy val referenceType = stringProperty("referencesType")
}