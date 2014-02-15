package de.jowisoftware.neo4j.database

import java.io.File

import scala.collection.JavaConversions.mapAsJavaMap

import org.apache.commons.configuration.{ Configuration, MapConfiguration }
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.configuration.Configurator
import org.neo4j.server.configuration.Configurator.Adapter
import org.neo4j.server.database.CommunityDatabase

class EmbeddedDatabaseWithConsole(filepath: File)
    extends AbstractEmbeddedDatabase(filepath) {

  private var server: CommunityDatabase = _

  protected override def init(): GraphDatabaseService = {
    val configurator = new Adapter() {
      override def getDatabaseTuningProperties =
        AbstractEmbeddedDatabase.defaultSettings.map(e => e._1.name -> e._2).toMap[String, String]

      override def configuration: Configuration =
        new MapConfiguration(Map(Configurator.DATABASE_LOCATION_PROPERTY_KEY -> filepath.getAbsolutePath()))
    }

    server = new CommunityDatabase(configurator)
    server.start()
    server.getGraph()
  }

  override def shutdown() =
    if (server != null)
      server.stop()
}
