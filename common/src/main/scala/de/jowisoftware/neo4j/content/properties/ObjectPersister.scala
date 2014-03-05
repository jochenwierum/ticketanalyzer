package de.jowisoftware.neo4j.content.properties

private[properties] trait ObjectPersister[T] {
  protected[properties] def obj2Persist(obj: T): Any
  protected[properties] def persist2Obj(persist: Any): T
}
