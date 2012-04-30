package de.jowisoftware.mining.importer.async

trait ConsoleProgressReporter extends AsyncDatabaseImportHandler {
  private var lastTotal = -1L

  override def reportProgress(ticketsDone: Long, ticketsCount: Long,
    commitsDone: Long, commitsCount: Long) {

    val tp = if (ticketsCount == 0) 0 else 1000 * ticketsDone / ticketsCount
    val cp = if (commitsCount == 0) 0 else 1000 * commitsDone / commitsCount
    val total = if (ticketsCount + commitsCount == 0) 0
    else 1000 * (ticketsDone + commitsDone) / (commitsCount + ticketsCount)

    if (lastTotal != total) {
      println("%.1f %% done: %d of %s Tickets (%.1f %%), %d of %s Commits (%.1f %%)".
        format(total / 10.0, ticketsDone, num(ticketsCount), tp / 10.0,
          commitsDone, num(commitsCount), cp / 10.0))
      lastTotal = total
    }

    super.reportProgress(ticketsDone, ticketsCount, commitsDone, commitsCount)
  }

  private def num(x: Long) =
    if (x <= 0) "?"
    else x.toString
}