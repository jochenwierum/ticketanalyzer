package de.jowisoftware.mining.test

trait MockHelper {
  def withMocks[A](block: MockContext => A): Unit = {
    val context = new MockContext
    block(context)
  }
}