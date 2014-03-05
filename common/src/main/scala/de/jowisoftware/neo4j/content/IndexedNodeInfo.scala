package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.Label

case class IndexedNodeInfo(label: Label, properties: List[String])