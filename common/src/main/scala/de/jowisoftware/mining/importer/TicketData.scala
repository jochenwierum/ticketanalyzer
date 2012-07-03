package de.jowisoftware.mining.importer

import java.util.Date
import scala.collection.SortedMap
import TicketDataFields._

object TicketData {
  def apply(id: Int) = {
    val result = new TicketData()
    result(TicketDataFields.id) = id
    result
  }
}

class TicketData(reference: TicketData) extends FieldList(TicketDataFields) {
  def this() = this(null)

  if (reference != null)
    reference.data.foreach { data += _ }
}