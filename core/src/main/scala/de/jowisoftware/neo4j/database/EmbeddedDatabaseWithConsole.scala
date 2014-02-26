package de.jowisoftware.neo4j.database

import java.io.File
import scala.collection.JavaConversions.mapAsJavaMap
import org.apache.commons.configuration.{ Configuration, MapConfiguration }
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.configuration.Configurator
import org.neo4j.server.configuration.Configurator.Adapter
import org.neo4j.server.database.CommunityDatabase
import org.neo4j.server.CommunityNeoServer

class EmbeddedDatabaseWithConsole(filepath: File)
    extends AbstractEmbeddedDatabase(filepath) {

  private var server: CommunityNeoServer = _

  protected override def init(): GraphDatabaseService = {
    val configurator = new Adapter() {
      override def getDatabaseTuningProperties = {
        val javamap = new java.util.HashMap[String, String]()
        AbstractEmbeddedDatabase.defaultSettings.foreach(e => javamap.put(e._1.name, e._2))
        javamap
      }

      override def configuration: Configuration = {
        val javamap = new java.util.HashMap[String, String]()
        javamap.put(Configurator.DATABASE_LOCATION_PROPERTY_KEY, filepath.getAbsolutePath())
        new MapConfiguration(javamap)
      }
    }

    server = new CommunityNeoServer(configurator)
    server.start()
    server.getDatabase().getGraph()
  }

  override def shutdown() =
    if (server != null)
      server.stop()
}
