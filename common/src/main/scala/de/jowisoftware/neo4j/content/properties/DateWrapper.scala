package de.jowisoftware.neo4j.content.properties

import java.util.Date
import java.text.SimpleDateFormat

private[neo4j] object DateWrapper {
  val FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
}

private[neo4j] trait DateWrapper extends ObjectPersister[Date] {
  def obj2Persist(obj: Date): Any = DateWrapper.FORMAT.format(obj)
  def persist2Obj(persist: Any): Date = DateWrapper.FORMAT.parse(persist.toString)
}
