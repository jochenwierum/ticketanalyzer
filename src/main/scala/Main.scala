import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.{Evaluation => NeoEvaluation}
import de.jowisoftware.neo4j._
import de.jowisoftware.neo4j.Traverser._
import de.jowisoftware.neo4j.Relationship._

import scala.collection.JavaConversions._

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
      println()
      
      var d = Traverser()
      d = d.breadthFirst()
      d = d.relationships(PersonRel)
      d = d.evaluator((p: Path) => NeoEvaluation.INCLUDE_AND_CONTINUE)
      println(d.traverse(root).nodes.map{Node.neoNode2Node(_)})
      
      /*
      println(person.neighbors().map{_.toString})
      println(person.neighbors(Direction.INCOMING).map{_.toString})
      println(person.neighbors(Direction.OUTGOING).map{_.toString})
      */
      
      dbit.success
    }
  }
}