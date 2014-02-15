package de.jowisoftware.mining.test

import org.scalatest.exceptions.TestFailedException

trait MockHelper {
  private def rewriteExceptions[A](block: => A): A =
    try {
      block
    } catch {
      case e: AssertionError =>
        val result = new TestFailedException(Option(e.getMessage),
          Option(e.getCause), 0)
        result.setStackTrace(e.getStackTrace)
        throw result.severedAtStackDepth
    }

  def withMocks[A](block: MockContext => A): Unit = {
    rewriteExceptions {
      val context = new MockContext
      block(context)
    }
  }
}