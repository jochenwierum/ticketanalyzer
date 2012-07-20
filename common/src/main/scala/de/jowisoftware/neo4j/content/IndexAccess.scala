package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.{ Node => NeoNode }
import de.jowisoftware.neo4j.DBWithTransaction
import scala.collection.JavaConversions._
import de.jowisoftware.neo4j.ReadOnlyDatabase
import de.jowisoftware.neo4j.ReadWriteDatabase

object IndexAccess {
  def mask(s: String) =
    s.replaceAll("""([-+!(){}\[\]^"~*?:\\]|&&|\|\|)""", "\\\\$1")
}

trait IndexAccess[A <: Node] {
  import IndexAccess._

  private def getIndex(db: ReadOnlyDatabase[_ <: Node])(implicit manifest: Manifest[A]): Index[NeoNode] =
    db.service.index.forNodes(manifest.erasure.getSimpleName)

  protected def findInIndex(db: ReadOnlyDatabase[_ <: Node], indexName: String, value: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]): Option[A] = {
    val result = getIndex(db)(manifest).query(indexName, value).getSingle
    Option(result).map(Node.wrapNeoNode(_, db, companion))
  }

  protected def findInIndex(db: ReadOnlyDatabase[_ <: Node], query: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]): Option[A] = {
    val result = getIndex(db)(manifest).query(query).getSingle
    Option(result).map(Node.wrapNeoNode(_, db, companion))
  }

  protected def findMultipleInIndex(db: ReadOnlyDatabase[_ <: Node], indexName: String, value: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]) =
    getIndex(db)(manifest).query(indexName, value).iterator.map { result => Node.wrapNeoNode(result, db, companion) }

  protected def findMultipleInIndex(db: ReadOnlyDatabase[_ <: Node], query: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]) =
    getIndex(db)(manifest).query(query).iterator.map { result => Node.wrapNeoNode(result, db, companion) }
}