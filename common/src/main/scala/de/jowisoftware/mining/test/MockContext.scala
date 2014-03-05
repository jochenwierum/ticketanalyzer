package de.jowisoftware.mining.test

import java.io.PrintWriter
import scala.language.implicitConversions
import scala.reflect.runtime.universe._
import org.mockito.Mockito
import org.mockito.internal.invocation.Invocation
import org.mockito.invocation.InvocationOnMock
import org.mockito.listeners.InvocationListener
import org.mockito.listeners.MethodInvocationReport
import org.mockito.stubbing.Answer
import org.mockito.stubbing.OngoingStubbing
import org.neo4j.cypher.ExecutionResult
import org.neo4j.cypher.PlanDescription
import org.neo4j.cypher.QueryStatistics
import org.neo4j.graphdb.ResourceIterable
import org.neo4j.graphdb.ResourceIterator
import grizzled.slf4j.Logging

class MockContext private[test] extends Logging {
  class DebugListener(name: String) extends InvocationListener {
    def reportInvocation(methodInvocationReport: MethodInvocationReport): Unit = {
      if (methodInvocationReport.getReturnedValue() == null) {
        val location = methodInvocationReport.getInvocation.getLocation.toString
        if (!(location.contains("de.jowisoftware.mining.test") || location.matches(".*Test($|[$.]).*"))) {
          val invocation = methodInvocationReport.getInvocation.asInstanceOf[Invocation]
          val name = invocation.getMethod().getName()
          val args = invocation.getArguments().map(_ match {
            case null => "null"
            case s: String => s
            case a @ (Int | Float | Short | Double | Char | Byte) => a.toString()
            case o => o.getClass.getSimpleName
          }).mkString("(", ", ", ")")
          warn(s"Found possible problem in test: Invocation $name$args yielded null! Location: $location")
        }
      }
    }
  }

  class ThenReturnSingle[T](inner: OngoingStubbing[T]) {
    def thenReturnSingle(o: T): OngoingStubbing[T] = {
      inner.thenReturn(o, Seq[T](): _*)
    }
  }

  implicit def returnFix[T](stub: OngoingStubbing[T]) = new ThenReturnSingle[T](stub)

  implicit def blockToAnswer[T](block: InvocationOnMock => T) = new Answer[T] {
    def answer(invocation: InvocationOnMock): T = block(invocation)
  }

  def mock[A <: AnyRef: TypeTag](name: String = "")(implicit manifest: Manifest[A]): A = {
    def mock(classObject: Class[_], mockName: String) =
      Mockito.mock(classObject.asInstanceOf[Class[A]],
          Mockito.withSettings()
            .name(mockName)
            .invocationListeners(new DebugListener(mockName)))

    val erased = runtimeMirror(getClass.getClassLoader).runtimeClass(typeOf[A])
    val obj = if (name == "")
      mock(erased, erased.getSimpleName)
    else
      mock(erased, name.replaceAll("""[^A-Za-z0-9_$]""", """\$"""))

    obj
  }

  def resourceIterable[A](values: A*): ResourceIterable[A] = new ResourceIterable[A] {
    def iterator = resourceIterator(values: _*)
  }

  def resourceIterator[A](values: A*): ResourceIterator[A] = {
    val inner = values.iterator

    new ResourceIterator[A] {
      def next() = inner.next
      def hasNext() = inner.hasNext
      def close() = {}
      def remove() = ???
    }
  }

  def executionResult(values: Map[String, Any]*): ExecutionResult = {
    val inner = values.iterator

    new ExecutionResult {
      def hasNext = inner.hasNext
      def next = inner.next

      def columns: List[String] = ???
      def javaColumns: java.util.List[String] = ???
      def javaColumnAs[T](column: String): ResourceIterator[T] = ???
      def columnAs[T](column: String): Iterator[T] = ???
      def javaIterator: ResourceIterator[java.util.Map[String, Any]] = ???
      def dumpToString(writer: PrintWriter) = ???
      def dumpToString(): String = ???
      def queryStatistics(): QueryStatistics = ???
      def executionPlanDescription(): PlanDescription = ???
      def close() = {}
    }
  }
}
