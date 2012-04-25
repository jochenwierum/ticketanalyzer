package de.jowisoftware.mining.gui

import scala.swing.Panel

abstract class AssistantPage {
  def title: String
  def getPanel: Panel

  def save(): Boolean = true
}