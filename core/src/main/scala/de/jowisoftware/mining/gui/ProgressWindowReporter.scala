package de.jowisoftware.mining.gui

import scala.swing.Swing

import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler

trait ProgressbarReporter extends AsyncDatabaseImportHandler {
  private var firstRun = true
  protected var dialog: ProgressDialog

  override def reportProgress = {
    Swing.onEDT {
      dialog.max = ticketsCount + commitsCount
      dialog.progress = ticketsDone + commitsDone
    }
    super.reportProgress
  }
}