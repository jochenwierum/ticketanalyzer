package de.jowisoftware.mining.linker

trait LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String)
  def finish()

  def foundLink(link: Link)
}