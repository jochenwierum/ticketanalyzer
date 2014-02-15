package de.jowisoftware.mining.model.nodes

import scala.collection.JavaConversions.iterableAsScalaIterable

import org.neo4j.graphdb.{ Direction, DynamicLabel }

import de.jowisoftware.mining.model.relationships.Contains
import de.jowisoftware.neo4j.content.NodeCompanion
import helper._

object RootNode extends NodeCompanion[RootNode] {
  val graphVersion = 3
  val labelFunction = DynamicLabel.label("function")
  def apply = new RootNode
}

class RootNode extends MiningNode {
  val version = 2

  def updateFrom(version: Int) = {
    if (version < 2) {
      this.content.addLabel(RootNode.labelFunction)
      this.function("rootNode")

      for (relationship <- content.getRelationships()) {
        val node = relationship.getEndNode()
        relationship.delete()
        node.delete()
      }
    }
  }

  override def initProperties = {
    info("Root node was created")
    state(0)
    this.content.addLabel(RootNode.labelFunction)
    graphVersion(RootNode.graphVersion)
  }

  def updateRequired = graphVersion() < RootNode.graphVersion
  def updateFinished = graphVersion(RootNode.graphVersion)

  val state = intProperty("state")
  val function = stringProperty("function", "rootNode")
  val graphVersion = intProperty("graphVersion", RootNode.graphVersion)
}
