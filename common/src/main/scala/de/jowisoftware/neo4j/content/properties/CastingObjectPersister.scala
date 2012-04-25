package de.jowisoftware.neo4j.content.properties

private[neo4j] trait CastingObjectPersister[T] extends ObjectPersister[T] {
  def obj2Persist(obj: T): Any = obj
  def persist2Obj(persist: Any): T = persist.asInstanceOf[T]
}