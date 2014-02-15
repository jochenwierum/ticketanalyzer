package de.jowisoftware.util

import java.io.Closeable

object ScalaUtil {
  class CloseableWrapper[A <: AutoCloseable] private[ScalaUtil] (private val closeable: A) {
    def execute[B](block: A => B): B = {
      try {
        block(closeable)
      } finally {
        if (closeable != null) {
          try {
            closeable.close()
          } catch {
            case _: Exception =>
          }
        }
      }
    }
  }

  def withClosable[A <: AutoCloseable](init: => A) = new CloseableWrapper[A](init)
}