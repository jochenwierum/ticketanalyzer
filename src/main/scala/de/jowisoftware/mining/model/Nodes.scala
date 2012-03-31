package de.jowisoftware.mining.model
import _root_.de.jowisoftware.neo4j._
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.RelationshipType

protected trait HasNameCompanion[T <: HasName] extends NodeCompanion[T] {
  protected def findOrCreate(db: DBWithTransaction[RootNode], name: String,
      relationShip: RelationshipCompanion[_ <: Relationship], parent: Node): T = {
    
    val neighbor = parent.neighbors(Direction.OUTGOING, List(relationShip.relationType)).find{
      _ match {
        case node: HasName => node.name() == name
      }
    }
    
    neighbor match {
      case Some(node) => node.asInstanceOf[T]
      case None =>
        val node = db.createNode(this)
        node.name(name)
        parent.add(node)(relationShip)
        node
    }
  }
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
  
  val schemaVersion = intProperty("schemaVersion")
  
  override def initProperties {
    schemaVersion(0)
  }
  
  private def getCollection[T <: Node](implicit nodeType: NodeCompanion[T]): T = {
    val node = getFirstRelationship(Direction.OUTGOING, Contains.relationType)(nodeType)
    
    node match {
      case Some(node) => node
      case _ => sys.error("Database is not properly initialized")
    }
  }
  
  lazy val statusCollection: Node = getCollection(StatusRepository)
  lazy val componentCollection: Node = getCollection(ComponentRepository)
  lazy val versionCollection: Node = getCollection(VersionRepository)
  lazy val typeCollection: Node = getCollection(TypeRepository)
  lazy val milestoneCollection: Node = getCollection(MilestoneRepository)
  lazy val ticketRepositoryCollection: Node = getCollection(TicketRepository)
}



object Ticket extends NodeCompanion[Ticket] {
  def apply = new Ticket
} 

class Ticket extends Node {
  val version = 1
  def updateFrom(version: Int) = {}
  
  val id = stringProperty("id")
  val reporter = stringProperty("reporter")
  val text = stringProperty("text")
  val title = stringProperty("title")

  val owner = stringProperty("owner") 
}



object TicketRepositoryRepository extends NodeCompanion[TicketRepositoryRepository] {
  def apply = new TicketRepositoryRepository
}

class TicketRepositoryRepository extends Node with EmptyNode

object TicketRepository extends NodeCompanion[TicketRepository] with HasNameCompanion[TicketRepository] {
  def apply = new TicketRepository
  
  def findOrCreate(db: DBWithTransaction[RootNode], name: String): TicketRepository =
    findOrCreate(db, name, Contains, db.rootNode.ticketRepositoryCollection)
}

class TicketRepository extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object ComponentRepository extends NodeCompanion[ComponentRepository] {
  def apply = new ComponentRepository
}

class ComponentRepository extends Node with EmptyNode

object Component extends NodeCompanion[Component] with HasNameCompanion[Component] {
  def apply = new Component
  
  def findOrCreate(db: DBWithTransaction[RootNode], name: String): Component =
    findOrCreate(db, name, InComponent, db.rootNode.componentCollection)
}

class Component extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object Person extends NodeCompanion[Person] {
  def apply = new Person
}

class Person extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object VersionRepository extends NodeCompanion[VersionRepository] {
  def apply = new VersionRepository
}

class VersionRepository extends Node with EmptyNode

object Version extends NodeCompanion[Version] with HasNameCompanion[Version] {
  def apply = new Version
  
  def findOrCreate(db: DBWithTransaction[RootNode], name: String): Version =
    findOrCreate(db, name, InVersion, db.rootNode.versionCollection)
}

class Version extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object TypeRepository extends NodeCompanion[TypeRepository] {
  def apply = new TypeRepository
}

class TypeRepository extends Node with EmptyNode

object Type extends NodeCompanion[Type] with HasNameCompanion[Type] {
  def apply = new Type
  
  def findOrCreate(db: DBWithTransaction[RootNode], name: String): Type =
    findOrCreate(db, name, HasType, db.rootNode.typeCollection)
}

class Type extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object MilestoneRepository extends NodeCompanion[MilestoneRepository] {
  def apply = new MilestoneRepository
}

class MilestoneRepository extends Node with EmptyNode

object Milestone extends NodeCompanion[Milestone] with HasNameCompanion[Milestone] {
  def apply = new Milestone
  
  def findOrCreate(db: DBWithTransaction[RootNode], name: String): Milestone =
    findOrCreate(db, name, InMilestone, db.rootNode.milestoneCollection)
}

class Milestone extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object StatusRepository extends NodeCompanion[StatusRepository] {
  def apply = new StatusRepository
}

class StatusRepository extends Node with EmptyNode

object Status extends NodeCompanion[Status] with HasNameCompanion[Status] {
  def apply = new Status
  
  def findOrCreate(db: DBWithTransaction[RootNode], name: String): Status =
    findOrCreate(db, name, HasStatus, db.rootNode.statusCollection)
}

class Status extends Node with HasName {
  val version = 1
  def updateFrom(version: Int) = {}
}



object Commit extends NodeCompanion[Commit] {
  def apply = new Commit
}

class Commit extends Node {
  val version = 1
  def updateFrom(version: Int) = {}
  
  val comment = stringProperty("comment")
  val committer = stringProperty("committer")
  val id = stringProperty("id")
}