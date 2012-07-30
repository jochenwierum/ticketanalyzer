package de.jowisoftware.mining.importer.redmine

import de.jowisoftware.mining.importer.TicketRelationship

object RedmineParser {
  def parseRelation(text: String) = text match {
    case "precedes" => TicketRelationship.RelationshipType.precedes
    case "duplicates" => TicketRelationship.RelationshipType.duplicates
    case "blocks" => TicketRelationship.RelationshipType.blocks
    case "relates" => TicketRelationship.RelationshipType.references
  }
}