package de.jowisoftware.neo4j.content

trait IndexedNodeCompanion[A <: Node] extends NodeCompanion[A] {
  val indexInfo: IndexedNodeInfo

  def cypherForAll(nodeName: String) = s"($nodeName:${indexInfo.label.name()})"
  def cypherFor(nodeName: String, value: String) =
    s"($nodeName:${indexInfo.label.name()}{${indexInfo.indexProperty}:'$value'})"
}