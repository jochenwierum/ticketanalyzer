package de.jowisoftware.mining.shell

import java.io.File
import javax.swing.UIManager

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

    new MainUI(dbDir).run()
  }

  private def usageAndExit() {
    System.err.println("Usage: scala -cp... de.jowisoftware.minig.shell.Main db_path")
    System.exit(1)
  }
}