package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.model.nodes.Commit
import de.jowisoftware.mining.model.relationships.ChildOf
import de.jowisoftware.neo4j.DBWithTransaction
import de.jowisoftware.neo4j.content.Node
import de.jowisoftware.neo4j.traversing.Traverser
import de.jowisoftware.neo4j.traversing.Traverser.evaluator2NeoEvaluator
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.{Direction, Path, Node => NeoNode}

import scala.collection.JavaConversions._

class Neo4jRangeGenerator(db: DBWithTransaction) extends RangeGenerator {
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
