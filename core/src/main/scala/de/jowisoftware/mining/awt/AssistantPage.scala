package de.jowisoftware.mining.awt

import scala.swing.Panel

abstract class AssistantPage {
  def title: String
  def getPanel: Panel

  def save(): Boolean = true
}