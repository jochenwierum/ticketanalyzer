package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.{ Transaction => NeoTransaction }

private[neo4j] class AutonomousTransaction(
    db: Database,
    tx: NeoTransaction) extends DefaultTransaction(db, tx) {

  override def success = {
    super.success
    tx.close
  }

  override def failure = {
    super.failure
    tx.close
  }
}