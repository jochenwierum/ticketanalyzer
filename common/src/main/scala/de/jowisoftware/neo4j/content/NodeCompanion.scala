package de.jowisoftware.neo4j.content

import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.{ Node => NeoNode }

trait NodeCompanion[T <: Node] {
  def apply(): T
  def getIndex(db: DBWithTransaction[_])(implicit manifest: Manifest[T]): Index[NeoNode] =
    db.service.index.forNodes(manifest.erasure.getSimpleName)
}
