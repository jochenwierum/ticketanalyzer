package de.jowisoftware.mining.gui.linker

import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.gui.ProgressDialog

class LinkerEventGui(progressDialog: ProgressDialog) extends LinkEvents {
  def progress(progress: Long, max: Long, message: String) {
    progressDialog.update(progress, max)
    progressDialog.status(message)
  }

  def finish() {}
}