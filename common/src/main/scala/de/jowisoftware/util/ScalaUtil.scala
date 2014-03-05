package de.jowisoftware.util

import java.io.Closeable

object ScalaUtil {
  def withClosable[A <: AutoCloseable, B](init: => A)(block: A => B) = {
    val closeable = init
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