import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import de.jowisoftware.neo4j._

object RelTypes {
  case class ScalaRelationshipType(val name: String) extends RelationshipType
  val KNOWS = ScalaRelationshipType("knows")
  val PERSON = ScalaRelationshipType("person")
}

object Person extends NodeCompanion[Person] {
  def apply() = new Person()
} 

class Person extends Node {
  val version = 1
  
  val firstName = stringProperty("firstName")
  val lastName = stringProperty("lastName")
  
  override def initProperties = {
    firstName("John")
    lastName("Doe")
  }
  
  def updateFrom(oldVersion: Int) = {}
}

object RootNode extends NodeCompanion[RootNode] {
  def apply() = new RootNode()
}

class RootNode extends Node {
  val version = 1
  def updateFrom(oldVersion: Int) = {}
}

object Knows extends RelationshipCompanion[Knows] {
  val relationType = RelTypes.KNOWS
  
  type leftType = Person
  type rightType = Person
  
  def apply() = new Knows
}

class Knows extends Relationship {
  val version = 1
  def updateFrom(oldVersion: Int) = {}
}

object PersonRel extends RelationshipCompanion[PersonRel] {
  val relationType = RelTypes.PERSON
  
  type leftType = RootNode
  type rightType = Person
  
  def apply() = new PersonRel
}

class PersonRel extends Relationship {
  val version = 1
  def updateFrom(oldVersion: Int) = {}
}

object Main {
  import RelTypes._
  
  val dbPath = "db"
    
  def main(args: Array[String]) {
    Database.drop(dbPath)
    val database = Database(dbPath)
    
    try {
      doWork(database)
    } finally {
      database.shutdown
    }
  }
  
  def doWork(db: Database) {
    db.inTransaction { implicit dbit =>
      implicit val p = Person
      implicit val pr = PersonRel
      
      val root = dbit.rootNode(RootNode)
      val person: Person = dbit.createNode
      val person2: Person = dbit.createNode
      
      person.firstName("Jochen")
      person.lastName("Wierum")
      
      person2.firstName("Karl")
      person2.lastName("Klammer")
      
      val rel = person.add(person2)(Knows)
      root.add(person)
      root.add(person2)
      
      println(rel)
      println(rel.sink)
      
      println(root.neighbors(RelTypes.PERSON).map{_.toString})
      
      dbit.success
    }
  }
}