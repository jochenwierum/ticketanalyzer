package de.jowisoftware.mining.linker.trac

import scala.collection.JavaConversions._
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
import de.jowisoftware.neo4j.ReadOnlyDatabase

class Neo4jRangeGenerator(db: ReadOnlyDatabase) extends RangeGenerator {
  def findRange(c1: Commit, c2: Commit): Set[Commit] = {
    val (start, end) = if (c1.rank() >= c2.rank()) {
      (c1, c2)
    } else {
      (c2, c1)
    }

    var nodes: Set[NeoNode] = Set()

    Traverser(db.service)
      .depthFirst
      .relationships(ChildOf.relationType, Direction.OUTGOING)
      .evaluator {
        path: Path =>
          val currentCommit = Node.wrapNeoNode(path.endNode, db, Commit)

          val tmp = (currentCommit.rank() > end.rank(), currentCommit.equals(end)) match {
            case (true, _) => Evaluation.EXCLUDE_AND_CONTINUE
            case (false, true) =>
              nodes ++= path.nodes
              Evaluation.EXCLUDE_AND_PRUNE
            case (false, false) => Evaluation.EXCLUDE_AND_PRUNE
          }
          tmp
      }
      .traverse(start.content).foreach { _ => }

    nodes map { node => Node.wrapNeoNode(node, db, Commit) }
  }
}