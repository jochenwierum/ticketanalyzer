package de.jowisoftware.mining.importer

import scala.reflect.ClassTag

import scala.reflect.runtime.universe._

abstract class FieldListData {
  class FieldDescription[A: TypeTag] private[importer] (val name: String, val default: A) {
    val valueClass = typeOf[A]
    override def toString = name+"["+valueClass.toString()+"] = ("+default.toString+")"
  }

  protected def field[T](name: String, value: T)(implicit manifest: Manifest[T]) = {
    val result = new FieldDescription(name, value)
    fieldList = result :: fieldList
    result
  }

  private var fieldList: List[FieldDescription[_]] = List()
  def fields = fieldList
}
