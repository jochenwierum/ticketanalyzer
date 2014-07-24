package de.jowisoftware.neo4j.content

import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.graphdb.{Node => NeoNode}

trait RegexIndexAccess[A <: Node] { this: IndexedNodeCompanion[A] =>
  private def regexCypher(property: String) =
    s"MATCH (n:${indexInfo.label.name()}) WHERE n.$property =~ {value} RETURN n"

  protected def findByPatternInIndex(db: DBWithTransaction, property: String, value: String): Option[A] = {
    db.cypher(regexCypher(property)+" LIMIT 1",
      Map("value" -> value)).map(result => Node.wrapNeoNode(result("n").asInstanceOf[NeoNode], db, this)).toList match {
        case Nil => None
        case head :: tail => Some(head)
      }
  }

  protected def findMultipleByPatternInIndex(db: DBWithTransaction, property: String, value: String) = {
    db.cypher(regexCypher(property),
      Map("value" -> value)).map(
        result => Node.wrapNeoNode(result("n").asInstanceOf[NeoNode], db, this)).toList
  }
}
