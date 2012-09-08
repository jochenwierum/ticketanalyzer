package de.jowisoftware.neo4j.content

import scala.collection.JavaConversions.asScalaIterator

import org.neo4j.graphdb.{ Node => NeoNode }
import org.neo4j.graphdb.index.Index

import de.jowisoftware.neo4j.ReadOnlyDatabase
import grizzled.slf4j.Logging

object IndexAccess {
  def mask(s: String) =
    s.replaceAll("""([-+!(){}\[\]^"~*?:\\]|&&|\|\|)""", "\\\\$1")
}

trait IndexAccess[A <: Node] extends Logging {
  import IndexAccess._

  private def getIndex(db: ReadOnlyDatabase[_ <: Node])(implicit manifest: Manifest[A]): Index[NeoNode] =
    db.service.index.forNodes(manifest.erasure.getSimpleName)

  protected def findInIndex(db: ReadOnlyDatabase[_ <: Node], indexName: String, value: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]): Option[A] = {
    val result = debugIllegalQuery(indexName+", "+value, manifest.erasure.getSimpleName) {
      getIndex(db)(manifest).query(indexName, value).getSingle
    }
    Option(result).map(Node.wrapNeoNode(_, db, companion))
  }

  protected def findInIndex(db: ReadOnlyDatabase[_ <: Node], query: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]): Option[A] = {
    val result = debugIllegalQuery(query, manifest.erasure.getSimpleName) {
      getIndex(db)(manifest).query(query).getSingle
    }
    Option(result).map(Node.wrapNeoNode(_, db, companion))
  }

  protected def findMultipleInIndex(db: ReadOnlyDatabase[_ <: Node], indexName: String, value: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]) =
    debugIllegalQuery(indexName+", "+value, manifest.erasure.getSimpleName) {
      getIndex(db)(manifest).query(indexName, value).iterator.map { result => Node.wrapNeoNode(result, db, companion) }
    }

  protected def findMultipleInIndex(db: ReadOnlyDatabase[_ <: Node], query: String, companion: NodeCompanion[A])(implicit manifest: Manifest[A]) =
    debugIllegalQuery(query, manifest.erasure.getSimpleName) {
      getIndex(db)(manifest).query(query).iterator.map { result => Node.wrapNeoNode(result, db, companion) }
    }

  private def debugIllegalQuery[T](arg: String, indexName: String)(code: => T): T =
    try {
      code
    } catch {
      case e: Exception =>
        error("Could not execute query("+arg+") in index"+indexName, e)
        throw new RuntimeException(e)
    }
}