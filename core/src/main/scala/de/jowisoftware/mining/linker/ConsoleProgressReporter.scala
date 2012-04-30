package de.jowisoftware.mining.linker

trait ConsoleProgressReporter extends LinkEvents {
  private var lastTotal = 0L

  override abstract def reportProgress(progress: Long, max: Long, message: String): Unit = {
    val total = if (max > 0) 1000 * progress / max else 0

    if (lastTotal != total) {
      println("%s: %.1f %% done: %d of %s Operations".
        format(message, total / 10.0, num(progress), num(progress), num(max)))
      lastTotal = total
    }

    super.reportProgress(progress, max, message)
  }

  private def num(x: Long) =
    if (x <= 0) "?"
    else x.toString
}