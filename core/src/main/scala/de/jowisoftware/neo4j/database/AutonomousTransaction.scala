package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.{ Transaction => NeoTransaction }
import de.jowisoftware.neo4j.CypherService

private[neo4j] class AutonomousTransaction(
    db: Database,
    tx: NeoTransaction,
    cypherService: CypherService) extends DefaultTransaction(db, tx, cypherService) {

  override def success = {
    super.success
    tx.close
  }

  override def failure = {
    super.failure
    tx.close
  }
}