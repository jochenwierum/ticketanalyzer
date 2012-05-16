package de.jowisoftware.mining.importer

import de.jowisoftware.mining.importer.TicketRelationship.RelationshipType

object TicketRelationship {
  object RelationshipType extends Enumeration {
    val duplicates, duplicatedBy, references, parentOf, childOf = Value
  }
}

case class TicketRelationship(toTicket: String, TicketRelationship: RelationshipType.Value)