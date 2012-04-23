package de.jowisoftware.neo4j.content.properties

import de.jowisoftware.neo4j.content.Properties

abstract class NodeProperty[T] private[neo4j](protected[properties] val parent: Properties, protected[properties] val name: String)
    extends ObjectPersister[T] {
  def apply(newValue: T) = {
    parent.content.setProperty(name, obj2Persist(newValue))
  }

  def apply(): T = persist2Obj(parent.content.getProperty(name))

  override def hashCode = apply().hashCode
  override def equals(other: Any) = apply().equals(other)

  override def toString = apply().toString
}