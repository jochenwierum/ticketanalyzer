package de.jowisoftware.mining.awt

import scala.util.Random
import scala.swing.Panel
import scala.swing.BoxPanel
import scala.swing.Orientation

class TestPage extends AssistantPage {
  def title = "Titel "+Random.nextInt
  def getPanel = new BoxPanel(Orientation.Horizontal)
}