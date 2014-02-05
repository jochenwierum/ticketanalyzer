package de.jowisoftware.neo4j.database

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import org.neo4j.graphdb.{ Transaction => NeoTransaction }

private[neo4j] class AutonomousTransaction[T <: Node](
    db: Database[T],
    tx: NeoTransaction,
    rootCompanion: NodeCompanion[T]) extends DefaultTransaction[T](db, tx, rootCompanion) {

  override def success = {
    super.success
    tx.close
  }

  override def failure = {
    super.failure
    tx.close
  }
}