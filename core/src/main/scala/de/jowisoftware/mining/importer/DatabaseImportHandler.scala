package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class DatabaseImportHandler(protected val db: Database)
  extends GeneralImportHelper with TicketImportHandler with CommitImportHandler
