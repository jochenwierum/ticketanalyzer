package de.jowisoftware.mining.gui

import scala.swing.Swing

import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler

trait ProgressbarReporter extends AsyncDatabaseImportHandler {
  private var firstRun = true
  protected var dialog: ProgressDialog

  override def reportProgress = {
    val max = ticketsCount + commitsCount
    val state = ticketsDone + commitsDone
    dialog.update(state, max)
    super.reportProgress
  }
}