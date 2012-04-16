package de.jowisoftware.mining.importer

import scala.swing.Panel

trait ImporterOptions {
  def getPanel(): Panel
  def getUserInput: Map[String, String]
}