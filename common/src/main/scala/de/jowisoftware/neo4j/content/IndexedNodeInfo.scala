package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.Label
import org.neo4j.graphdb.DynamicLabel

object IndexedNodeInfo {
  def apply(label: String, indexProperty: String = "name"): IndexedNodeInfo =
    apply(DynamicLabel.label(label), indexProperty)
}

case class IndexedNodeInfo(
  label: Label,
  indexProperty: String)