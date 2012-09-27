package de.jowisoftware.mining.model.nodes

import org.neo4j.graphdb.Direction
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.mining.model.relationships.RootOf
import de.jowisoftware.mining.model.relationships.Updates
import java.util.GregorianCalendar
import java.util.Calendar

trait TicketUpdates { this: Ticket =>
  protected def updateToV4() {
    val cal = new GregorianCalendar()
    cal.setTime(this.updateDate())

    if (cal.get(Calendar.YEAR) < 80) {
      cal.add(Calendar.YEAR, 2000)
      this.updateDate(cal.getTime())
    } else if (cal.get(Calendar.YEAR) < 100) {
      cal.add(Calendar.YEAR, 1900)
      this.updateDate(cal.getTime())
    }
  }

  protected def updateToV3() {
    for (rootRel <- getFirstRelationship(Direction.BOTH, RootOf)) {
      rootRel.delete()
    }
    updateParent()
  }

  protected def updateToV2() = updateParent()

  private def updateParent() {
    def findRoot(e: Node): Node =
      e.neighbors(Direction.OUTGOING, Seq(Updates.relationType)).toList match {
        case Nil => e
        case x :: tail => findRoot(x)
      }

    val parent = findRoot(this)
    if (parent != this)
      parent.add(this, RootOf)
  }
}