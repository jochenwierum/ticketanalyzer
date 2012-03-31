package de.jowisoftware.mining.model
import de.jowisoftware.neo4j.Database
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.NodeCompanion
import de.jowisoftware.neo4j.Node

object Initialization {
  def initDB(db: Database[RootNode]) {
    db.inTransaction{transaction: DBWithTransaction[RootNode] => 
      val rootNode = transaction.rootNode
      
      if (rootNode.schemaVersion != 1) {
        createCollection(transaction, StatusRepository, rootNode)
        createCollection(transaction, ComponentRepository, rootNode)
        createCollection(transaction, VersionRepository, rootNode)
        createCollection(transaction, TypeRepository, rootNode)
        createCollection(transaction, MilestoneRepository, rootNode)
        createCollection(transaction, TicketRepository, rootNode)
        
        rootNode.schemaVersion(1)
      }
      
      transaction.success
    }
  }
  
  def createCollection[T <: Node](transaction: DBWithTransaction[_], nodeCompanion: NodeCompanion[T], rootNode: RootNode) {
    val node = transaction.createNode(nodeCompanion)
    rootNode.add(node)(Contains)
  }
}