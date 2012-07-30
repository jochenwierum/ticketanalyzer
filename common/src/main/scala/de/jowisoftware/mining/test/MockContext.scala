package de.jowisoftware.mining.test

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

class MockContext private[test] {
  class ThenReturnSingle[T](inner: OngoingStubbing[T]) {
    def thenReturnSingle(o: T): OngoingStubbing[T] = {
      inner.thenReturn(o, Seq[T](): _*)
    }
  }

  implicit def returnFix[T](stub: OngoingStubbing[T]) = new ThenReturnSingle[T](stub)

  implicit def blockToAnswer[T](block: InvocationOnMock => T) = new Answer[T] {
    def answer(invocation: InvocationOnMock): T = block(invocation)
  }

  def mock[A <: AnyRef](name: String = "")(implicit manifest: Manifest[A]): A = {
    val obj = if (name == "")
      Mockito.mock(manifest.erasure.asInstanceOf[Class[A]], manifest.erasure.getSimpleName)
    else {
      val cleanName = name.replaceAll("""[^A-Za-z0-9_$]""", """\$""")
      Mockito.mock(manifest.erasure.asInstanceOf[Class[A]], cleanName)
    }

    obj
  }
}
