package de.jowisoftware.mining.importer

abstract class FieldListData {
  class FieldDescription[A] private[importer] (val name: String, val default: A)(implicit manifest: Manifest[A]) {
    val valueClass = manifest.erasure
    override def toString = name+"["+valueClass.getSimpleName+"] = ("+default.toString+")"
  }

  protected def field[T](name: String, value: T)(implicit manifest: Manifest[T]) = {
    val result = new FieldDescription(name, value)
    fieldList = result :: fieldList
    result
  }

  private var fieldList: List[FieldDescription[_]] = List()
  def fields = fieldList
}
