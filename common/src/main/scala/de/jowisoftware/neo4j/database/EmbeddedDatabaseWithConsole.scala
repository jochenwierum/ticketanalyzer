package de.jowisoftware.neo4j.database

import scala.collection.JavaConversions.iterableAsScalaIterable
import org.neo4j.kernel.EmbeddedGraphDatabase
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.content.NodeCompanion
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.DBWithTransaction
import org.neo4j.server.WrappingNeoServerBootstrapper
import org.neo4j.server.configuration.ServerConfigurator
import org.neo4j.server.configuration.Configurator

class EmbeddedDatabaseWithConsole[T <: Node](filepath: String, rootCompanion: NodeCompanion[T]) extends EmbeddedDatabase[T](filepath, rootCompanion) {
  private val server = new WrappingNeoServerBootstrapper(service)
  server.start

  override def shutdown() {
    server.stop()
    super.shutdown()
  }
}
