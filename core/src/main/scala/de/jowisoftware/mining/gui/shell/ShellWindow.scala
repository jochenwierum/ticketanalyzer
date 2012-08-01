package de.jowisoftware.mining.gui.shell

import scala.swing.Frame
import org.neo4j.kernel.AbstractGraphDatabase
import scala.swing.event.WindowClosing
import java.awt.Dimension

class ShellWindow(db: AbstractGraphDatabase) extends Frame {
  title = "Database Shell"
  contents = new ShellPane(db, this)

  reactions += {
    case WindowClosing(_) =>
      db.shutdown()
      dispose()
  }

  def run() {
    size = new Dimension(640, 480)
    visible = true
  }
}
