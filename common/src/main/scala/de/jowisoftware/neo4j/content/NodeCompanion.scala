package de.jowisoftware.neo4j.content

import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.{ Node => NeoNode }

trait NodeCompanion[T <: Node] {
  def apply(): T

  private def getIndex(db: DBWithTransaction[_])(implicit manifest: Manifest[T]): Index[NeoNode] =
    db.service.index.forNodes(manifest.erasure.getSimpleName)

  protected def findInIndex[A <: Node](db: DBWithTransaction[A], indexName: String, value: String)
    (implicit manifest: Manifest[T]): Option[T] = {

    val result = getIndex(db)(manifest).query(indexName, value).getSingle

    if (result == null)
      None
    else
      Some(Node.wrapNeoNode(result, db)(this))
  }
}
