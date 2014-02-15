package de.jowisoftware.neo4j.database

import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.{ Node => NeoNode }
import de.jowisoftware.neo4j.{ Database, DatabaseCollection }
import de.jowisoftware.neo4j.content.{ IndexedNodeCompanion, Node }
import de.jowisoftware.util.ScalaUtil.withClosable
import org.neo4j.graphdb.ResourceIterator

class DefaultDatabaseCollection(db: Database) extends DatabaseCollection {
  private val executionEngine = new ExecutionEngine(db.service)

  def find[A <: Node](companion: IndexedNodeCompanion[A], name: String): Option[A] = {
    val nodes = db.service.findNodesByLabelAndProperty(companion.indexInfo.label, companion.indexInfo.indexProperty, name)
    withClosable[ResourceIterator[NeoNode]](nodes.iterator()) execute { it =>
      if (it.hasNext())
        Some(Node.wrapNeoNode(it.next, db, companion))
      else
        None
    }
  }

  def findOrCreate[A <: Node](companion: IndexedNodeCompanion[A], name: String): A = {
    find(companion, name) match {
      case Some(value) => value
      case None =>
        Node.wrapNeoNode(db.service.createNode(), db, companion)
    }
  }

  def findAll[A <: Node](companion: IndexedNodeCompanion[A]): Iterator[A] = {
    val query = "START n = node:"+companion.indexInfo.label.name()+"('*:*') RETURN n"
    executionEngine.execute(query).map(result =>
      Node.wrapNeoNode(result.get("n").asInstanceOf[NeoNode], db, companion))
  }
}