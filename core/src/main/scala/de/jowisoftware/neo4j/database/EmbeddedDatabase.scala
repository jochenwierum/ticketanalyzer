package de.jowisoftware.neo4j.database

import java.io.File

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.{ GraphDatabaseBuilder, GraphDatabaseFactory }

class EmbeddedDatabase(filepath: File) extends AbstractEmbeddedDatabase(filepath) {
  protected def init(): GraphDatabaseService = {
    val builder = new GraphDatabaseFactory()
      .newEmbeddedDatabaseBuilder(filepath.getAbsolutePath)

    for ((setting, value) <- AbstractEmbeddedDatabase.defaultSettings)
      builder.setConfig(setting, value)

    builder.newGraphDatabase()
  }

  def shutdown(): Unit =
    try {
      if (service != null)
        service.shutdown()
    } catch {
      case e: Exception =>
      // we can't do anything here, so we ignore the exception
    }
}
