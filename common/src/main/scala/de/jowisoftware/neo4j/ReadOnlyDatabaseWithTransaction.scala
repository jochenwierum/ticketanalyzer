package de.jowisoftware.neo4j

trait ReadOnlyDatabaseWithTransaction extends ReadOnlyDatabase {
  def inTransaction[S](body: DBWithTransaction => S): S
}
