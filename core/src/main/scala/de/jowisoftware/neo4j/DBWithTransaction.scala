package de.jowisoftware.neo4j

import content.{Node, NodeCompanion}
import org.neo4j.kernel.AbstractGraphDatabase

trait DBWithTransaction[T <: Node] {
  def success
  def failure

  val rootNode: T = getRootNode
  protected def getRootNode: T
  
  private[neo4j] def service: AbstractGraphDatabase

  def createNode[T <: Node](implicit companion: NodeCompanion[T]): T
}