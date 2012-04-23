package de.jowisoftware.neo4j

import content.{Node, NodeCompanion}

trait DBWithTransaction[T <: Node] {
  def success
  def failure

  val rootNode: T = getRootNode
  protected def getRootNode: T

  def createNode[T <: Node](implicit companion: NodeCompanion[T]): T
}