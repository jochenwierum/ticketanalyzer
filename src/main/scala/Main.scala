import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import de.jowisoftware.neo4j._

object RelTypes {
  case class ScalaRelationshipType(val name: String) extends RelationshipType
  val KNOWS = ScalaRelationshipType("knows")
  val PERSON = ScalaRelationshipType("person")
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
  
  //val friends = neighbors(RelTypes.KNOWS)
}

class RootNode extends Node {
  val version = 1
  def updateFrom(oldVersion: Int) = {}
}

class Knows extends Relationship {
  val relationType = RelTypes.KNOWS
  protected type leftType = Person
  protected type rightType = Person
  protected val leftTypeManifest = manifest[leftType]
  protected val rightTypeManifest = manifest[rightType]
  
  val version = 1
  def updateFrom(oldVersion: Int) = {}
}

class PersonRel extends Relationship {
  val relationType = RelTypes.PERSON
  protected type leftType = RootNode
  protected type rightType = Person
  protected val leftTypeManifest = manifest[leftType]
  protected val rightTypeManifest = manifest[rightType]
  
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
      val root = dbit.rootNode[RootNode]
      val person = dbit.createNode[Person]
      val person2 = dbit.createNode[Person]
      
      person.firstName("Jochen")
      person.lastName("Wierum")
      
      person2.firstName("Karl")
      person2.lastName("Klammer")
      
      val rel = person.add[Knows](person2)
      root.add[PersonRel](person)
      root.add[PersonRel](person2)
      
      println(rel)
      println(rel.sink)
      
      val test2 = dbit.createNode
      val test = root.add(person)
      println(root.neighbors(RelTypes.PERSON).map{_.toString})
      
      dbit.success
    }
  }
}