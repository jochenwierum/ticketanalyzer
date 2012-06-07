package de.jowisoftware.neo4j.database

import org.neo4j.server.WrappingNeoServerBootstrapper
import de.jowisoftware.neo4j.content.{ NodeCompanion, Node }
import java.io.File

class EmbeddedDatabaseWithConsole[T <: Node](filepath: File, rootCompanion: NodeCompanion[T])
    extends EmbeddedDatabase[T](filepath, rootCompanion) {
  private var server: WrappingNeoServerBootstrapper = _

  override protected def init() = {
    super.init

    server = new WrappingNeoServerBootstrapper(service)
    server.start
  }

  override def shutdown() {
    server.stop(0)
    super.shutdown()
  }
}
