package de.jowisoftware.mining.gui.linker

import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.gui.ProgressDialog

trait LinkerEventGui extends LinkEvents {
  val progressDialog: ProgressDialog

  abstract override def reportProgress(progress: Long, max: Long, message: String) {
    progressDialog.update(progress, max)
    progressDialog.status = message

    super.reportProgress(progress, max, message)
  }
}