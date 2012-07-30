package de.jowisoftware.mining.linker.trac

import scala.collection.JavaConversions.iterableAsScalaIterable
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.{ Node => NeoNode }
import de.jowisoftware.mining.model.nodes.{ RootNode, Commit }
import de.jowisoftware.mining.model.relationships.ChildOf
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.traversing.Traverser
import de.jowisoftware.neo4j.traversing.Traverser.evaluator2NeoEvaluator
import org.neo4j.graphdb.Direction

trait RangeGenerator {
  def findRange(c1: Commit, c2: Commit): Set[Commit]
}