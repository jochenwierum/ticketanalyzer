package de.jowisoftware.mining.importer

import scala.collection.SortedMap

abstract class FieldList {
  protected val fieldListData: FieldListCompanion#FieldListData

  protected var values: Map[fieldListData.FieldDescription[_], (Any, String)] =
    fieldListData.fields.map(field => (field -> (field.default, ""))).toMap

  def update[T](field: fieldListData.FieldDescription[T], value: (T, String)) =
    values += field -> value

  def apply[T](field: fieldListData.FieldDescription[T]): T =
    values(field)._1.asInstanceOf[T]

  override def toString = getClass().getSimpleName+"(\n"+(
    SortedMap.empty(Ordering.by { k: fieldListData.FieldDescription[_] => k.name }) ++ values).map {
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