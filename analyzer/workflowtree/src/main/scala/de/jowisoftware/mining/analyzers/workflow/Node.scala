package de.jowisoftware.mining.analyzers.workflow

import scala.collection.mutable

class Node(val name: String, val label: String) extends Printable {
  private var _count: Int = 0
  private var _finalCount: Int = 0
  private var _ignoredCount: Int = 0
  private var draw = true
  var factor: Double = 0f

  private var relations: List[Relation] = Nil

  def count = _count

  def setCounts(c: Int) {
    _count = c
    _finalCount = c
  }

  def addNonFinal(c: Int) = _finalCount -= c
  def addIgnored(c: Int) = _ignoredCount += c

  def incrementCounts() {
    _count += 1
    _finalCount += 1
  }

  def hide() = draw = false
  def children = relations
  def addChild(child: Node) =
    relations = new Relation(child) :: relations

  def toStringBuilder(builder: mutable.StringBuilder, highlight: Boolean) {
    // this condition is only important for root-Elements
    if (draw) {
      builder append name append
        """ [label="""" append label append """\ntotal: """ append
        _count append " (" append perCent(factor) append ")" append
        """\nfinal: """ append perCentWithLabel(_finalCount, count) append
        """\nignored: """ append perCentWithLabel(_ignoredCount, count) append
        "\"" append (if (highlight) ", color = red, fontcolor = red" else "") append "];\n"

      val highlightedChild = relations.reduceOption((r1, r2) =>
        if (r1.factor > r2.factor) r1 else r2).flatMap(r =>
        if (r.factor < _finalCount.floatValue() / count) None else Some(r))

      relations.foreach { relation =>
        val highlightNode = highlight &&
          highlightedChild.map(_ == relation).getOrElse(false)

        if (relation.to.draw) {
          relation.to.toStringBuilder(builder, highlightNode)
          relation.toStringBuilder(builder, name, highlightNode)
        }
      }
    }
  }
}