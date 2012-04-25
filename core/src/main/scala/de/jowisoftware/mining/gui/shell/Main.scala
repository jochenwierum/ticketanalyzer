package de.jowisoftware.mining.gui.shell

import java.io.File
import javax.swing.UIManager
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase

object Main {
  def main(args: Array[String]) {
    if (args.length != 1)
      usageAndExit()

    val dbDir = new File(args(0))
    if (!dbDir.isDirectory)
      usageAndExit()

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch {
      case e: Exception =>
    }

    val db = new EmbeddedReadOnlyGraphDatabase(dbDir.getAbsolutePath)
    new ShellWindow(db).run()
  }

  private def usageAndExit() {
    System.err.println("Usage: scala -cp... de.jowisoftware.minig.shell.Main db_path")
    System.exit(1)
  }
}