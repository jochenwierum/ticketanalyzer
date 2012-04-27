package de.jowisoftware.mining.gui

import de.jowisoftware.mining.importer.async.AsyncDatabaseImportHandler
import scala.swing.Dialog
import scala.swing.Frame
import javax.swing.SwingUtilities

trait ProgressbarReporter extends AsyncDatabaseImportHandler {
  private var firstRun = true
  protected var dialog: ProgressDialog

  override def reportProgress = {
    SwingUtils.invokeAsync {
      dialog.max = ticketsCount + commitsCount
      dialog.progress = ticketsDone + commitsDone
    }
    super.reportProgress
  }
}