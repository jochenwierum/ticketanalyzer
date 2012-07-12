package de.jowisoftware.neo4j.content

import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.{ Node => NeoNode }

import scala.collection.JavaConversions._

trait NodeCompanion[T <: Node] {
  def apply(): T

  private def getIndex(db: DBWithTransaction[_])(implicit manifest: Manifest[T]): Index[NeoNode] =
    db.service.index.forNodes(manifest.erasure.getSimpleName)

  protected def findInIndex[A <: Node](db: DBWithTransaction[A], indexName: String, value: String)(implicit manifest: Manifest[T]): Option[T] = {
    val result = getIndex(db)(manifest).query(indexName, value).getSingle
    Option(result).map(Node.wrapNeoNode(_, db, this))
  }

  protected def findInIndex[A <: Node](db: DBWithTransaction[A], query: String)(implicit manifest: Manifest[T]): Option[T] = {
    val result = getIndex(db)(manifest).query(query).getSingle
    Option(result).map(Node.wrapNeoNode(_, db, this))
  }

  protected def findMultipleInIndex[A <: Node](db: DBWithTransaction[A], indexName: String, value: String)(implicit manifest: Manifest[T]) =
    getIndex(db)(manifest).query(indexName, value).iterator.map { result => Node.wrapNeoNode(result, db, this) }

  protected def findMultipleInIndex[A <: Node](db: DBWithTransaction[A], query: String)(implicit manifest: Manifest[T]) =
    getIndex(db)(manifest).query(query).iterator.map { result => Node.wrapNeoNode(result, db, this) }
}
