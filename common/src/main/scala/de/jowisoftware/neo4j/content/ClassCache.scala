package de.jowisoftware.neo4j.content

private[content] class ClassCache[T] {
  private var classCache: Map[String, T] = Map()

  protected def getCompanion(className: String)(implicit man: Manifest[T]): T =
    classCache.getOrElse(className, findAndCacheCompanion(className))

  private def findAndCacheCompanion(className: String)(implicit man: Manifest[T]): T = {
    val classObject = Class.forName(className + "$")
    val companion = classObject.getField("MODULE$").get(man.erasure).asInstanceOf[T]
    classCache += className -> companion
    companion
  }
}