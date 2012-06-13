package de.jowisoftware.neo4j.content.properties

import de.jowisoftware.neo4j.content.Properties
import org.neo4j.graphdb.PropertyContainer
import de.jowisoftware.neo4j.content.index.Index

abstract class NodeProperty[A, B <: PropertyContainer] private[neo4j](
      parent: Properties[B],
      name: String,
      default: A,
      index: Index)
    extends ObjectPersister[A]{

  def apply(newValue: A) = {
    require(newValue != null, "Can't set null value - use OptionalNodeProperty instead")
    val value = obj2Persist(newValue)
    parent.content.setProperty(name, value)
    index.index(value)
  }

  def apply(): A = if (parent.content.hasProperty(name))
      persist2Obj(parent.content.getProperty(name))
    else
      default

  override def hashCode = apply().hashCode
  override def equals(other: Any) = apply().equals(other)

  override def toString = apply().toString
}
