package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.{CypherService, Database}
import org.neo4j.graphdb.{Transaction => NeoTransaction}

private[neo4j] class AutonomousTransaction(
    db: Database,
    tx: NeoTransaction,
    cypherService: CypherService) extends DefaultTransaction(db, tx, cypherService) {

  override def success() = {
    super.success()
    tx.close()
  }

  override def failure = {
    super.failure()
    tx.close()
  }
}
