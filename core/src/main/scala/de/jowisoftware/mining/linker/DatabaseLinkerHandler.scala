package de.jowisoftware.mining.linker

class DatabaseLinkerHandler extends LinkEvents {
  def reportProgress(progress: Long, max: Long, message: String): Unit = {}
  def finish(): Unit = {}

  def foundLink(link: Link): Unit = {

  }
}