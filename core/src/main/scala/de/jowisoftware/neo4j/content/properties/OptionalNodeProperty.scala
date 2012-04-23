package de.jowisoftware.neo4j.content.properties

import de.jowisoftware.neo4j.content.Properties

abstract class OptionalNodeProperty[T](val parent: Properties, val name: String)
    extends ObjectPersister[T] {
  def apply(newValue: Option[T]): Unit = {
    newValue match {
      case Some(x) => parent.content.setProperty(name, obj2Persist(x))
      case None => parent.content.removeProperty(name)
    }
  }

  def apply(): Option[T] = {
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
