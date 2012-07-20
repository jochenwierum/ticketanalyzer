package de.jowisoftware.neo4j

import org.neo4j.kernel.AbstractGraphDatabase

import content.Node

trait Database[T <: Node] extends ReadOnlyDatabase[T] {
  def shutdown
  def inTransaction[S](body: DBWithTransaction[T] => S): S
  def startTransaction: DBWithTransaction[T]
  def deleteContent

  def service: AbstractGraphDatabase
}