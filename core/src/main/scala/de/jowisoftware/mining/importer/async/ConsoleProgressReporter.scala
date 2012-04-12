package de.jowisoftware.mining.importer.async

trait ConsoleProgressReporter extends AsyncDatabaseImportHandler {
  private var lastTotal = -1L
  
  def reportProgress {
    val tp = if (ticketsCount == 0) 0 else 1000 * ticketsDone / ticketsCount
    val cp = if (commitsCount == 0) 0 else 1000 * commitsDone / commitsCount
    val total = if (ticketsCount + commitsCount == 0) 0
      else 1000 * (ticketsDone + commitsDone) / (commitsCount + ticketsCount) 
    
    if (lastTotal != total) {
      println(mkStatusLine(tp, cp, total))
      lastTotal = total
    }
  }
  
  private def mkStatusLine(tp: Long, cp: Long, total: Long) =
    "%.1f %% done: %d of %s Tickets (%.1f %%), %d of %s Commits (%.1f %%)".
      format(total / 10.0, ticketsDone, num(ticketsCount), tp / 10.0,
          commitsDone, num(commitsCount), cp / 10.0);

  private def num(x: Long) =
    if (x <= 0) "?"
    else x.toString
}