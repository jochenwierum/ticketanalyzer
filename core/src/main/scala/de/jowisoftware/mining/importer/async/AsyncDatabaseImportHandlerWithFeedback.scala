package de.jowisoftware.mining.importer.async

import de.jowisoftware.mining.gui.importer.ImporterEventGui
import akka.actor.ActorSystem
import de.jowisoftware.neo4j.Database
import de.jowisoftware.mining.importer.Importer
import akka.actor.Props
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.AkkaHelper

private case class RunWithUI(db: Database[RootNode],
  dialog: ProgressDialog,
  importer: (Importer, Map[String, String])*) extends ImportEvent

object AsyncDatabaseImportHandlerWithFeedback {
  def run(db: Database[RootNode],
    dialog: ProgressDialog,
    importer: (Importer, Map[String, String])*) {

    val importHandler = AkkaHelper.system.actorOf(Props[AsyncDatabaseImportHandler],
      name = "asyncDatabaseImportHandler")

    importHandler ! RunWithUI(db, dialog, importer: _*)
  }
}

class AsyncDatabaseImportHandlerWithFeedback extends ConsoleProgressReporter with ImporterEventGui {
  protected var dialog: ProgressDialog = _

  override def receive: PartialFunction[Any, Unit] = {
    case RunWithUI(db, dialog, importer) =>
      this.dialog = dialog
      self ! Run(db, importer)
    case _: ImportEvent =>
      super.receive
  }
}