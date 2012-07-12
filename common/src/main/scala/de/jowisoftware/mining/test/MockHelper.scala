package de.jowisoftware.mining.test

import org.scalatest.TestFailedException

trait MockHelper {
  private def rewriteExceptions[A](block: => A): A =
    try {
      block
    } catch {
      case e: AssertionError =>
        val skip = e.getStackTrace.indexWhere { frame =>
          frame.getMethodName == "invoke" &&
            frame.getClassName == "org.easymock.internal.ObjectMethodsFilter"
        } + 2

        val result = new TestFailedException(Option(e.getMessage),
          Option(e.getCause), skip)
        result.setStackTrace(e.getStackTrace)
        throw result.severedAtStackDepth
    }

  class CheckWord[A](context: MockContext, parameters: A) {
    def andCheck(block: A => Unit) = {
      rewriteExceptions {
        context.replay()
        block(parameters)
        context.verify()
      }
    }
  }

  def prepareMock[A](setup: MockContext => A): CheckWord[A] = {
    rewriteExceptions {
      val context = new MockContext
      val result = setup(context)
      new CheckWord[A](context, result)
    }
  }
}