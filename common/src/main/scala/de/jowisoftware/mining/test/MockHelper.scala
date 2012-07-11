package de.jowisoftware.mining.test

import org.scalatest.mock.JMockExpectations
import org.scalatest.TestFailedException

import org.easymock.EasyMock
import org.easymock.EasyMock._

trait MockHelper {
  class MockContext private[MockHelper] {
    private var newMocks: Set[Object] = Set()
    private var runningMocks: Set[Object] = Set()
    EasyMock.createControl

    def mock[A <: AnyRef](name: String = "")(implicit manifest: Manifest[A]): A = {
      val obj = if (name == "")
        createMock(manifest.erasure.getSimpleName, manifest.erasure.asInstanceOf[Class[A]])
      else
        createMock(name, manifest.erasure.asInstanceOf[Class[A]])

      newMocks += obj
      obj
    }

    def replay(mock: Object) = {
      EasyMock.replay(mock)
      newMocks -= mock
      runningMocks -= mock
    }

    private[MockHelper] def replay() = newMocks.foreach(EasyMock.replay(_))
    private[MockHelper] def verify() = runningMocks.foreach(EasyMock.verify(_))
  }

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