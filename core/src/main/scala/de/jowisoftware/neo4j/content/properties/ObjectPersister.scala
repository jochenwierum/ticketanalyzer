package de.jowisoftware.neo4j.content.properties

private[neo4j] trait ObjectPersister[T] {
  protected[properties] def obj2Persist(obj: T): Any
  protected[properties] def persist2Obj(persist: Any): T
}