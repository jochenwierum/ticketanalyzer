package de.jowisoftware.mining.importer

import scala.collection.SortedMap

abstract class FieldList[A <: FieldListData](val values: A) {
  protected var data: Map[values.FieldDescription[_], (Any, String)] =
    values.fields.map(field => (field -> (field.default, ""))).toMap

  def update[T](field: values.FieldDescription[T], value: (T, String)) =
    data += field -> value

  def apply[T](field: values.FieldDescription[T]): T =
    data(field)._1.asInstanceOf[T]

  override def toString = getClass().getSimpleName+"(\n"+(
    SortedMap.empty(Ordering.by { k: values.FieldDescription[_] => k.name }) ++ data).map {
      case (k, v) => "  "+k.name+"="+niceTupel(v)
    }.mkString(",\n")+"\n)"

  private def niceTupel(t: (Any, String)) = {
    (t._1 match {
      case s: String if s.length > 50 => "\""+s.substring(0, 47)+"...\""
      case s: String => "\""+s+"\""
      case x => x.toString
    }).replace("\n", "\\n") + (if (t._2.isEmpty) "" else " by "+t._2)
  }
}