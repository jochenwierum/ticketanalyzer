package de.jowisoftware.mining.test

import org.scalatest.exceptions.TestFailedException

trait MockHelper {
  def withMocks[A](block: MockContext => A): Unit = {
    val context = new MockContext
    block(context)
  }
}