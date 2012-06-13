package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model.RootNode
import de.jowisoftware.neo4j.DBWithTransaction

class DatabaseImportHandler(protected val db: DBWithTransaction[RootNode])
    extends GeneralImportHelper with TicketImportHandler with CommitImportHandler
