package de.jowisoftware.mining.analyzers.workflow

import scala.collection.mutable

class Relation(val to: Node) extends Printable {
  var factor: Double = 0f

  def toStringBuilder(builder: mutable.StringBuilder, from: String, highlight: Boolean) =
    builder append from append " -> " append to.name append
      " [label = \"" append perCent(factor)+"\"" append
      (if (highlight) ", color = red, fontcolor = red" else "") append
      "];\n"
}