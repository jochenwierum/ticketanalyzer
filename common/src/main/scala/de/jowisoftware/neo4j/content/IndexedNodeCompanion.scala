package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.{ DynamicLabel, Label, Node => NeoNode }
import de.jowisoftware.neo4j.{ ReadOnlyDatabase, ReadWriteDatabase }
import de.jowisoftware.util.ScalaUtil
import de.jowisoftware.neo4j.DBWithTransaction

trait IndexedNodeCompanion[A <: Node] extends NodeCompanion[A] {
  private val indexName = getClass().getSimpleName().replace("$", "")
  protected val indexedProperties: List[String] = Nil
  protected val primaryProperty: String

  lazy val indexInfo = new IndexedNodeInfo(DynamicLabel.label(indexName), primaryProperty :: indexedProperties)

  def cypherForAll(nodeName: String) = s"($nodeName:${indexInfo.label.name()})"
  def cypherFor(nodeName: String, property: String, value: String): String =
    s"($nodeName:${indexInfo.label.name()}{$property:'$value'})"
  def cypherFor(nodeName: String, value: String): String =
    cypherFor(nodeName, primaryProperty, value)

  def find(db: DBWithTransaction, name: String): Option[A] =
    db.cypher(s"MATCH (n:${indexInfo.label.name()}) WHERE n.${primaryProperty} = {value} RETURN n LIMIT 1",
      Map("value" -> name)).map(
        result => Node.wrapNeoNode(result("n").asInstanceOf[NeoNode], db, this)).toList match {
          case Nil => None
          case head :: tail => Some(head)
        }

  def findAll(db: DBWithTransaction): Seq[A] = {
    db.inTransaction {
      _.cypher(s"MATCH ${cypherForAll("n")} RETURN n").map {
        n => Node.wrapNeoNode(n("n").asInstanceOf[NeoNode], db, this)
      }.toSeq
    }
  }

  def findOrCreate(db: DBWithTransaction, name: String): A = {
    find(db, name) match {
      case Some(node) => node
      case None =>
        val node = db.createNode(this)
        node.content.setProperty(primaryProperty, name)
        node
    }
  }
}