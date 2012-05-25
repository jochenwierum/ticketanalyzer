package de.jowisoftware.mining.model
import de.jowisoftware.neo4j.content._
import de.jowisoftware.neo4j._
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.RelationshipType

trait Node extends de.jowisoftware.neo4j.content.Node {
  def id = content.getId
  def rootNode = db.rootNode.asInstanceOf[RootNode]
  override def db = super.db.asInstanceOf[DBWithTransaction[RootNode]]
}

protected trait HasChildWithName[T <: HasName] extends Node {
  protected def findOrCreateChild(name: String,
    relationShip: RelationshipCompanion[_ <: Relationship], creator: NodeCompanion[T]): T = {

    val neighbor = neighbors(Direction.OUTGOING, List(relationShip.relationType)).find {
      _ match {
        case node: HasName => node.name() == name
      }
    }

    neighbor match {
      case Some(node) => node.asInstanceOf[T]
      case None =>
        val node = db.createNode(creator)
        node.name(name)
        add(node)(relationShip)
        node
    }
  }

  def findOrCreateChild(name: String): T
}

protected trait HasName extends Node {
  val name = stringProperty("name")
}

protected trait EmptyNode extends Node {
  val version = 1
  def updateFrom(version: Int) = {}
}

object RootNode extends NodeCompanion[RootNode] {
  def apply = new RootNode
}

class RootNode extends Node {
  val version = 1
  def updateFrom(version: Int) = {}

  private def getCollection[T <: Node](implicit nodeType: NodeCompanion[T]): T = {
    val node = getFirstRelationship(Direction.OUTGOING, Contains.relationType)(nodeType)

    node match {
      case Some(node) => node
      case _ =>
        val node = db.createNode(nodeType)
        this.add(node)(Contains)
        node
    }
  }

  override def initProperties =
    state(0)

  val state = intProperty("state")
  lazy val statusCollection = getCollection(StatusRepository)
  lazy val componentCollection = getCollection(ComponentRepository)
  lazy val versionCollection = getCollection(VersionRepository)
  lazy val typeCollection = getCollection(TypeRepository)
  lazy val milestoneCollection = getCollection(MilestoneRepository)
  lazy val ticketRepositoryCollection = getCollection(TicketRepositoryRepository)
  lazy val personCollection = getCollection(PersonRepository)
  lazy val commitRepositoryCollection = getCollection(CommitRepositoryRepository)
  lazy val tagCollection = getCollection(TagRepository)
  lazy val resolutionCollection = getCollection(ResolutionRepository)
  lazy val priorityCollection = getCollection(PriorityRepository)
  lazy val severityCollection = getCollection(SeverityRepository)
  lazy val reproducabilityCollection = getCollection(ReproducabilityRepository)
}

object Ticket extends NodeCompanion[Ticket] {
  def apply = new Ticket
}

class Ticket extends Node {
  val version = 1
  def updateFrom(version: Int) = {}

  val ticketId = intProperty("id")
  val reporter = stringProperty("reporter")
  val text = stringProperty("text")
  val title = stringProperty("title")
  val tags = optionalStringProperty("tags")
  val updateDate = dateProperty("time")
  val creationDate = dateProperty("time")
  val votes = intProperty("votes")
  val eta = intProperty("eta")
  val environment = stringProperty("environment")
  val build = stringProperty("build")
}



object TicketRepositoryRepository extends NodeCompanion[TicketRepositoryRepository] {
  def apply = new TicketRepositoryRepository
}

class TicketRepositoryRepository extends Node with EmptyNode with HasChildWithName[TicketRepository] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, TicketRepository)
}

object TicketRepository extends NodeCompanion[TicketRepository] {
  def apply = new TicketRepository
}

class TicketRepository extends Node with HasName with EmptyNode {
  def createTicket(): Ticket = {
    val ticket = db.createNode(Ticket)
    this.add(ticket)(Contains)
    ticket
  }
}



object ComponentRepository extends NodeCompanion[ComponentRepository] {
  def apply = new ComponentRepository
}

class ComponentRepository extends Node with EmptyNode with HasChildWithName[Component] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Component)
}

object Component extends NodeCompanion[Component] {
  def apply = new Component
}

class Component extends Node with HasName with EmptyNode



object VersionRepository extends NodeCompanion[VersionRepository] {
  def apply = new VersionRepository
}

class VersionRepository extends Node with EmptyNode with HasChildWithName[Version] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Version)
}

object Version extends NodeCompanion[Version] {
  def apply = new Version
}

class Version extends Node with HasName with EmptyNode



object TypeRepository extends NodeCompanion[TypeRepository] {
  def apply = new TypeRepository
}

class TypeRepository extends Node with EmptyNode with HasChildWithName[Type] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Type)
}

object Type extends NodeCompanion[Type] {
  def apply = new Type
}

class Type extends Node with HasName with EmptyNode



object MilestoneRepository extends NodeCompanion[MilestoneRepository] {
  def apply = new MilestoneRepository
}

class MilestoneRepository extends Node with EmptyNode with HasChildWithName[Milestone] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Milestone)
}

object Milestone extends NodeCompanion[Milestone] {
  def apply = new Milestone
}

class Milestone extends Node with HasName with EmptyNode



object StatusRepository extends NodeCompanion[StatusRepository] {
  def apply = new StatusRepository
}

class StatusRepository extends Node with EmptyNode with HasChildWithName[Status] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Status)
}

object Status extends NodeCompanion[Status] {
  def apply = new Status
}

class Status extends Node with HasName with EmptyNode



object PersonRepository extends NodeCompanion[PersonRepository] {
  def apply = new PersonRepository
}

class PersonRepository extends Node with EmptyNode with HasChildWithName[Person] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Person)
}

object Person extends NodeCompanion[Person] {
  def apply = new Person
}

class Person extends Node with HasName with EmptyNode



object CommitRepositoryRepository extends NodeCompanion[CommitRepositoryRepository] {
  def apply = new CommitRepositoryRepository
}

class CommitRepositoryRepository extends Node with EmptyNode with HasChildWithName[CommitRepository] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, CommitRepository)
}

object CommitRepository extends NodeCompanion[CommitRepository] {
  def apply = new CommitRepository
}

class CommitRepository extends Node with HasName with EmptyNode {
  def createCommit(): Commit = db.createNode(Commit)
  def createFile(): File = db.createNode(File)

  def findFile(name: String): Option[File] = {
    neighbors(Direction.OUTGOING, Seq(Contains.relationType)) find {
      _ match {
        case f: File => f.name == name
        case _ => false
      }
    } match {
      case None => None
      case Some(file: File) => Some(file)
    }
  }
}



object Commit extends NodeCompanion[Commit] {
  def apply = new Commit
}

class Commit extends Node {
  val version = 1
  def updateFrom(version: Int) = {}

  val commitId = stringProperty("id")
  val message = stringProperty("message")
  val date = dateProperty("date")
}



object File extends NodeCompanion[File] {
  def apply = new File
}

class File extends Node with EmptyNode with HasName



object TagRepository extends NodeCompanion[TagRepository] {
  def apply = new TagRepository
}

class TagRepository extends Node with EmptyNode with HasChildWithName[Tag] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Tag)
}

object Tag extends NodeCompanion[Tag] {
  def apply = new Tag
}

class Tag extends Node with EmptyNode with HasName



object ResolutionRepository extends NodeCompanion[ResolutionRepository] {
  def apply = new ResolutionRepository
}

class ResolutionRepository extends Node with EmptyNode with HasChildWithName[Resolution] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Resolution)
}

object Resolution extends NodeCompanion[Resolution] {
  def apply = new Resolution
}

class Resolution extends Node with EmptyNode with HasName



object PriorityRepository extends NodeCompanion[PriorityRepository] {
  def apply = new PriorityRepository
}

class PriorityRepository extends Node with EmptyNode with HasChildWithName[Priority] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Priority)
}

object Priority extends NodeCompanion[Priority] {
  def apply = new Priority
}

class Priority extends Node with EmptyNode with HasName



object SeverityRepository extends NodeCompanion[SeverityRepository] {
  def apply = new SeverityRepository
}

class SeverityRepository extends Node with EmptyNode with HasChildWithName[Severity] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Severity)
}

object Severity extends NodeCompanion[Severity] {
  def apply = new Severity
}

class Severity extends Node with EmptyNode with HasName



object ReproducabilityRepository extends NodeCompanion[ReproducabilityRepository] {
  def apply = new ReproducabilityRepository
}

class ReproducabilityRepository extends Node with EmptyNode with HasChildWithName[Reproducability] {
  def findOrCreateChild(name: String) =
    findOrCreateChild(name, Contains, Reproducability)
}

object Reproducability extends NodeCompanion[Reproducability] {
  def apply = new Reproducability
}

class Reproducability extends Node with EmptyNode with HasName



object TicketComment extends NodeCompanion[TicketComment] {
  def apply = new TicketComment
}

class TicketComment extends Node {
  val version = 1
  def updateFrom(version: Int) {}
  
  val commentId = intProperty("id")
  val text = stringProperty("text", "", Some("comment-text"))
}