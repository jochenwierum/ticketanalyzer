package de.jowisoftware.mining.importer.async

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.gui.importer.ImporterEventGui
import de.jowisoftware.mining.importer.Importer
import de.jowisoftware.neo4j.Database
import grizzled.slf4j.Logging

private case class RunWithUI(db: Database,
  dialog: ProgressDialog,
  importer: (Importer, Map[String, String])*) extends ImportEvent

object AsyncDatabaseImportHandlerWithFeedback extends Logging {
  def run(db: Database,
    dialog: ProgressDialog,
    importer: (Importer, Map[String, String])*): Unit = {

    val config = ConfigFactory.empty()
        .withValue("akka.jvm-exit-on-fatal-error", ConfigValueFactory.fromAnyRef(false))
        .withValue("pinned.executor", ConfigValueFactory.fromAnyRef("thread-pool-executor"))
        .withValue("pinned.type", ConfigValueFactory.fromAnyRef("PinnedDispatcher"))
        .withValue("pinned.thread-pool-executor.allow-core-timeout", ConfigValueFactory.fromAnyRef("off"))
    val system = ActorSystem("MiningSystem", config)

    info(s"Starting import of ${importer.size} repositories using akka")
    val importHandler = system.actorOf(Props[AsyncDatabaseImportHandlerWithFeedback].withDispatcher("pinned"),
      name = "asyncDatabaseImportHandler")

    importHandler ! RunWithUI(db, dialog, importer: _*)

    system.awaitTermination()
    info(s"Akka finished")
  }
}

class AsyncDatabaseImportHandlerWithFeedback extends AsyncDatabaseImportHandler with ConsoleProgressReporter with ImporterEventGui {
  protected var dialog: ProgressDialog = _

  override def receive: PartialFunction[Any, Unit] = {
    case RunWithUI(db, dialogObj, importer) =>
      dialog = dialogObj
      self ! Run(db, importer)
    case e: ImportEvent =>
      super.receive(e)
  }
}
