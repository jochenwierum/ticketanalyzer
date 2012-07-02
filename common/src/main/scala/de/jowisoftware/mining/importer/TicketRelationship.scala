package de.jowisoftware.mining.importer

import de.jowisoftware.mining.importer.TicketRelationship.RelationshipType

object TicketRelationship {
  object RelationshipType extends Enumeration {
    val duplicates, duplicatedBy, references, parentOf, childOf, blocks, follows, precedes = Value
  }
}

case class TicketRelationship(toTicket: Int, ticketRelationship: RelationshipType.Value)
