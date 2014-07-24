package de.jowisoftware.mining.model.nodes

import de.jowisoftware.mining.model.nodes.helper._
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.DynamicLabel

import scala.collection.JavaConversions.iterableAsScalaIterable

object RootNode extends NodeCompanion[RootNode] {
  val graphVersion = 3
  val labelFunction = DynamicLabel.label("function")
  def apply() = new RootNode
}

class RootNode extends MiningNode {
  val version = 2

  def updateFrom(version: Int) = {
    if (version < 2) {
      this.content.addLabel(RootNode.labelFunction)
      this.function("rootNode")
      this.initialized(true)

      for (relationship <- content.getRelationships) {
        val node = relationship.getEndNode
        node.getRelationships.foreach(_.delete)
        println("Delete: "+node.getId+": "+node.getProperty("_class"))
        node.delete()
      }
    }
  }

  override def initProperties() = {
    info("Root node was created")
    state(0)
    this.content.addLabel(RootNode.labelFunction)
    graphVersion(RootNode.graphVersion)
    function("rootNode")
  }

  def updateRequired: Boolean = graphVersion() < RootNode.graphVersion
  def updateFinished(): Unit = graphVersion(RootNode.graphVersion)

  val state = intProperty("state")
  val function = stringProperty("function", "rootNode")
  val graphVersion = intProperty("graphVersion", RootNode.graphVersion)
  val initialized = booleanProperty("initialized", default = false)
}
