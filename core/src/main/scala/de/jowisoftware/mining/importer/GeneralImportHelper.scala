package de.jowisoftware.mining.importer

import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.mining.model.RootNode

private[importer] trait GeneralImportHelper extends ImportEvents {
  protected val db: DBWithTransaction[RootNode]
  protected lazy val root = db.rootNode

  protected def getPerson(name: String) = root.personCollection.findOrCreateChild(name)

  protected def getTicketRepository(name: String) =
    root.ticketRepositoryCollection.findOrCreateChild(name)
  protected def getCommitRepository(name: String) =
    root.commitRepositoryCollection.findOrCreateChild(name)

  def finish() {}
}