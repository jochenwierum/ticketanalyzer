package de.jowisoftware.mining.test

import org.easymock.EasyMock
import org.easymock.EasyMock._

class MockContext private[test] {
  private var newMocks: Set[Object] = Set()
  private var runningMocks: Set[Object] = Set()

  def mock[A <: AnyRef](name: String = "")(implicit manifest: Manifest[A]): A = {
    val obj = if (name == "")
      createMock(manifest.erasure.getSimpleName, manifest.erasure.asInstanceOf[Class[A]])
    else {
      val cleanName = name.replaceAll("""[^A-Za-z0-9_$]""", """\$""")
      createMock(cleanName, manifest.erasure.asInstanceOf[Class[A]])
    }

    newMocks += obj
    obj
  }

  def replay(mock: Object) = {
    EasyMock.replay(mock)
    newMocks -= mock
    runningMocks -= mock
  }

  private[test] def replay() = newMocks.foreach(EasyMock.replay(_))
  private[test] def verify() = runningMocks.foreach(EasyMock.verify(_))
}
