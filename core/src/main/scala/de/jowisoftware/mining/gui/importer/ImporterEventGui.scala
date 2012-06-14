package de.jowisoftware.mining.gui.importer

import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler

trait ImporterEventGui extends AsyncDatabaseImportHandler {
  private var firstRun = true
  protected var dialog: ProgressDialog

  override def reportProgress(ticketsDone: Long, ticketsCount: Long,
    commitsDone: Long, commitsCount: Long) {
    val max = ticketsCount + commitsCount
    val state = ticketsDone + commitsDone

    dialog.update(state, max)

    super.reportProgress(ticketsDone: Long, ticketsCount: Long,
      commitsDone: Long, commitsCount: Long)
  }
}