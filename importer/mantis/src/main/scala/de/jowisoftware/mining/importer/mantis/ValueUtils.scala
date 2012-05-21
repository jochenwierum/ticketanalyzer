package de.jowisoftware.mining.importer.mantis

import de.jowisoftware.mining.importer.TicketRelationship.RelationshipType

object ValueUtils {
  private val etaRegex = """([<>]?)\s*(\d+)\s*(.*)""".r
  private val etaRangeRegex = """(\d+)-(\d+)\s*(.*)""".r

  def etaStringToInt(eta: String): Int = eta match {
    case etaRegex(minMax, span, spanString) =>
      val base = span.toInt * stringToFactor(spanString)
      minMax match {
        case ">" => base + 1
        case _ => base
      }
    case etaRangeRegex(minSpan, maxSpan, spanString) => maxSpan.toInt * stringToFactor(spanString)
    case _ => 0
  }

  private def stringToFactor(s: String) = s match {
    case "hour" | "hours" => 1
    case "day" | "days" => 8
    case "week" | "weeks" => 5 * 8
    case "month" | "months" => 4 * 5 * 8
    case _ => 0
  }

  def relationshipStringToRelationshipType(s: String) = s match {
    case "related to" => Some(RelationshipType.references)
    case "duplicate of" => Some(RelationshipType.duplicates)
    case "has duplicate" => Some(RelationshipType.duplicatedBy)
    case "parent of" => Some(RelationshipType.parentOf)
    case "child of" => Some(RelationshipType.childOf)
    case _ => None
  }
}