package de.jowisoftware.mining.importer.trac

import java.util.Date

import de.jowisoftware.mining.importer._
import de.jowisoftware.mining.importer.TicketDataFields._
import de.jowisoftware.mining.importer.TicketData

trait Change {
  val date: Date
  def update(ticket: TicketData)
  def downgrade(ticket: TicketData)
}

class SimpleChange[T](val date: Date, field: FieldDescription[T], oldValue: T, newValue: T, user: String) extends Change {
  def update(ticket: TicketData) = ticket(field) = newValue -> user
  def downgrade(ticket: TicketData) = ticket(field) = oldValue -> user
}

class ArrayChange[T](val date: Date, field: FieldDescription[Seq[T]], oldValue: Option[T], newValue: Option[T], user: String) extends Change {
  private def remove(ticket: TicketData, value: T) = ticket(field) = ticket(field).filterNot(_ == value) -> user
  private def add(ticket: TicketData, value: T) = ticket(field) = (ticket(field) :+ value) -> user

  private def replace(ticket: TicketData, oldValue: T, newValue: T) =
    ticket(field) = ticket(field).map { value => if (value == oldValue) newValue else value } -> user

  def update(ticket: TicketData) =
    if (newValue == None)
      remove(ticket, oldValue.get)
    else if (oldValue == None)
      add(ticket, newValue.get)
    else
      replace(ticket, oldValue.get, newValue.get)

  def downgrade(ticket: TicketData) =
    if (oldValue == None)
      remove(ticket, newValue.get)
    else if (newValue == None)
      add(ticket, oldValue.get)
    else
      replace(ticket, newValue.get, oldValue.get)
}

class SetChange[T](val date: Date, field: FieldDescription[Seq[T]], oldValue: Set[T], newValue: Set[T], user: String) extends Change {
  def update(ticket: TicketData) =
    ticket(field) = (ticket(field).filterNot(oldValue.contains) ++ newValue) -> user

  def downgrade(ticket: TicketData) =
    ticket(field) = (ticket(field).filterNot(newValue.contains) ++ oldValue) -> user
}