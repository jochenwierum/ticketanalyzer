package de.jowisoftware.mining.importer

import scala.collection.SortedMap

abstract class FieldList[A <: FieldListData](val values: A) {
  protected var data: Map[values.FieldDescription[_], Any] =
    values.fields.map(field => (field -> field.default)).toMap

  var updatedFieldsSet: Set[String] = Set()

  def updatedFields = updatedFieldsSet

  def update[T](field: values.FieldDescription[T], value: T) = {
    updatedFieldsSet += field.name
    data += field -> value
  }

  def apply[T](field: values.FieldDescription[T]): T =
    data(field).asInstanceOf[T]

  override def toString = getClass().getSimpleName+"(\n"+(
    SortedMap.empty(Ordering.by { k: values.FieldDescription[_] => k.name }) ++ data).map {
      case (k, v) => "  "+k.name+"="+niceValue(v)
    }.mkString(",\n")+"\n)"

  private def niceValue(t: Any) =
    (t match {
      case s: String if s.length > 50 => "\""+s.substring(0, 47)+"...\""
      case s: String => "\""+s+"\""
      case x => x.toString
    }).replace("\n", "\\n")
}