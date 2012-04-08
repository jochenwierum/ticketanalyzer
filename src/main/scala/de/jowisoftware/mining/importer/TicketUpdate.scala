package de.jowisoftware.mining.importer
import java.util.Date

case class TicketUpdate(id: Int, field: String, newvalue: String,
  oldvalue: String="", author: String="", time: Date=new Date())