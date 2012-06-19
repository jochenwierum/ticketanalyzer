package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.Database

class DatabaseImportHandler(protected val db: Database[RootNode])
  extends GeneralImportHelper with TicketImportHandler with CommitImportHandler
