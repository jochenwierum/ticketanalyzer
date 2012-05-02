package de.jowisoftware.mining.importer
import java.util.Date

case class TicketData(repository: String, id: Int,
  summary: String = "", description: String = "",
  creationDate: Date = new Date(),
  updateDate: Date = new Date(), tags: String = "",
  reporter: String = "", version: String = "", ticketType: String = "",
  milestone: String = "", component: String = "", status: String = "", owner: String = "",
  resolution: String = "", blocking: String = "", priority: String = "",
  blocks: String = "", depends: String = "",
  updates: List[TicketUpdate] = List())