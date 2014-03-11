package de.jowisoftware.mining.gui.components;

import scala.collection.mutable._
import javax.swing._
import scala.swing._

class ToolBar extends Component with SequentialContainer.Wrapper {
  override lazy val peer: JToolBar = new JToolBar

  def buttons: Seq[Button] = contents.filter(_.isInstanceOf[Button]).map(_.asInstanceOf[Button])

  def addSeparator = peer.addSeparator()
}