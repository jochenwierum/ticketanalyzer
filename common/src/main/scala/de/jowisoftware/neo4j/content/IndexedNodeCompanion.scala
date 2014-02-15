package de.jowisoftware.neo4j.content

trait IndexedNodeCompanion[A <: Node] extends NodeCompanion[A] {
  val indexInfo: IndexedNodeInfo
}