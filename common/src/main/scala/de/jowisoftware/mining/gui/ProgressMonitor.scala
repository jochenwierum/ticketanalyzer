package de.jowisoftware.mining.gui

trait ProgressMonitor {
  def show(): Unit
  def hide(): Unit

  def max: Long
  def max_=(value: Long): Unit

  def progress: Long
  def progress_=(value: Long): Unit

  def tick(): Unit

  def update(value: Long, max: Long): Unit

  def status: String
  def status_=(text: String): Unit
}