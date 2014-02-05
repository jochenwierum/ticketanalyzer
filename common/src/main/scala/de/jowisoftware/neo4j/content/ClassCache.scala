package de.jowisoftware.neo4j.content

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

private[content] class ClassCache[T](implicit classTag: ClassTag[T]) {
  private var classCache: Map[String, T] = Map()

  protected def getCompanion(className: String): T =
    classCache.getOrElse(className, findAndCacheCompanion(className))

  private def findAndCacheCompanion(className: String): T = {
    val erased = classTag.runtimeClass
    val classObject = Class.forName(className+"$")
    val companion = classObject.getField("MODULE$").get(erased).asInstanceOf[T]
    classCache += className -> companion
    companion
  }
}