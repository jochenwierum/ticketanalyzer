package de.jowisoftware.neo4j.content

import scala.collection.JavaConversions.asScalaIterator
import org.neo4j.graphdb.{ Node => NeoNode }
import org.neo4j.graphdb.index.Index
import de.jowisoftware.neo4j.ReadOnlyDatabase
import grizzled.slf4j.Logging

import scala.reflect.runtime.universe._

object IndexAccess {
  def mask(s: String) =
    s.replaceAll("""([-+!(){}\[\]^"~*?:\\]|&&|\|\|)""", "\\\\$1")
}

trait IndexAccess[A <: Node] extends Logging {
  import IndexAccess._

  private def getIndex(db: ReadOnlyDatabase[_ <: Node])(implicit tag: TypeTag[A]): Index[NeoNode] = {
    val erased = runtimeMirror(getClass.getClassLoader).runtimeClass(typeOf[A])
    db.service.index.forNodes(erased.getSimpleName)
  }

  protected def findInIndex(db: ReadOnlyDatabase[_ <: Node], indexName: String, value: String, companion: NodeCompanion[A])(implicit tag: TypeTag[A]): Option[A] = {
    val erased = runtimeMirror(getClass.getClassLoader).runtimeClass(typeOf[A])
    val result = debugIllegalQuery(indexName+", "+value, erased.getSimpleName) {
      getIndex(db).query(indexName, value).getSingle
    }
    Option(result).map(Node.wrapNeoNode(_, db, companion))
  }

  protected def findInIndex(db: ReadOnlyDatabase[_ <: Node], query: String, companion: NodeCompanion[A])(implicit tag: TypeTag[A]): Option[A] = {
    val erased = runtimeMirror(getClass.getClassLoader).runtimeClass(typeOf[A])
    val result = debugIllegalQuery(query, erased.getSimpleName) {
      getIndex(db).query(query).getSingle
    }
    Option(result).map(Node.wrapNeoNode(_, db, companion))
  }

  protected def findMultipleInIndex(db: ReadOnlyDatabase[_ <: Node], indexName: String, value: String, companion: NodeCompanion[A])(implicit tag: TypeTag[A]) = {
    val erased = runtimeMirror(getClass.getClassLoader).runtimeClass(typeOf[A])
    debugIllegalQuery(indexName+", "+value, erased.getSimpleName) {
      getIndex(db).query(indexName, value).iterator.map { result => Node.wrapNeoNode(result, db, companion) }
    }
  }

  protected def findMultipleInIndex(db: ReadOnlyDatabase[_ <: Node], query: String, companion: NodeCompanion[A])(implicit tag: TypeTag[A]) = {
    val erased = runtimeMirror(getClass.getClassLoader).runtimeClass(typeOf[A])
    debugIllegalQuery(query, erased.getSimpleName) {
      getIndex(db).query(query).iterator.map { result => Node.wrapNeoNode(result, db, companion) }
    }
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