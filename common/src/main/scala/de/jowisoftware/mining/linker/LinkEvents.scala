package de.jowisoftware.mining.linker

trait LinkEvents {
  def progress(progress: Long, max: Long, message: String)
  def finish
}