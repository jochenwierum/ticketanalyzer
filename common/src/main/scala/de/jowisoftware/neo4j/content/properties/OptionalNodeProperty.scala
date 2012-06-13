package de.jowisoftware.neo4j.content.properties

import de.jowisoftware.neo4j.content.Properties
import org.neo4j.graphdb.PropertyContainer
import de.jowisoftware.neo4j.content.index.Index

abstract class OptionalNodeProperty[A, B <: PropertyContainer](
    parent: Properties[B],
    protected val name: String,
    index: Index)
    extends ObjectPersister[A] {
  def apply(newValue: Option[A]): Unit =
    newValue match {
      case Some(x) =>
        val value = obj2Persist(x)
        parent.content.setProperty(name, value)
        index.index(value)
      case None =>
        if (parent.content.hasProperty(name)) {
          parent.content.removeProperty(name)
          index.remove()
        }
    }

  def apply(): Option[A] = {
    if (parent.content.hasProperty(name))
      Some(persist2Obj(parent.content.getProperty(name)))
    else
      None
  }

  override def hashCode = apply() match {
    case Some(x) => x.hashCode()
    case None => 0
  }

  override def equals(other: Any) = apply() match {
    case Some(x) => x.equals(other)
    case None => other.equals(None)
  }

  override def toString = apply() match {
    case Some(x) => x.toString
    case None => "(Undefined)"
  }
}
