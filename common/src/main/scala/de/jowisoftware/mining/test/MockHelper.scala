package de.jowisoftware.mining.test

import org.jmock.Mockery
import org.scalatest.mock.JMockExpectations
import org.jmock.api.ExpectationError
import org.jmock.api.ExpectationErrorTranslator
import org.scalatest.TestFailedException

object MockHelper {
  def mock[A <: AnyRef](name: String = "")(implicit context: Mockery, manifest: Manifest[A]): A =
    if (name == "")
      context.mock(manifest.erasure.asInstanceOf[Class[A]], manifest.erasure.getSimpleName)
    else
      context.mock(manifest.erasure.asInstanceOf[Class[A]], name)

  def expecting(fun: JMockExpectations => Unit)(implicit context: Mockery) {
    val e = new JMockExpectations
    fun(e)
    context.checking(e)
  }
}

trait MockHelper {
  private object scalaTestErrorTranslator extends ExpectationErrorTranslator {
    def translate(e: ExpectationError): Error = {
      throw new TestFailedException(x => Option(e.toString()), Option(e), {
        ex =>
          ex.getStackTrace.indexWhere(el =>
            el.getFileName == "JavaReflectionImposteriser.java" &&
              el.getLineNumber == 33) + 2
      })
    }
  }

  class CheckWord[A](context: Mockery) {
    def andCheck(block: A => Unit) {
      block
      context.assertIsSatisfied
    }
  }

  def prepareMock[A](setup: Mockery => A): CheckWord[A] = {
    val context = new Mockery
    context.setExpectationErrorTranslator(scalaTestErrorTranslator)
    val result = setup(context)
    new CheckWord[A](context)
  }
}