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

class RangeGenerator(db: DBWithTransaction[RootNode]) {
  def findRange(c1: Commit, c2: Commit): Set[Commit] = {
    val (start, end) = if (c1.rank() >= c2.rank()) {
      (c1, c2)
    } else {
      (c2, c1)
    }

    val traverser = Traverser()
    traverser.breadthFirst
    traverser.relationships(ChildOf.relationType, Direction.OUTGOING)

    var nodes: Set[NeoNode] = Set()
    traverser.evaluator {
      path: Path =>
        val currentCommit = Node.wrapNeoNode(path.endNode, db, Commit)

        (currentCommit.rank() < c1.rank(), currentCommit.equals(end)) match {
          case (false, true) =>
            nodes ++= path.nodes
            Evaluation.EXCLUDE_AND_PRUNE
          case (false, false) => Evaluation.EXCLUDE_AND_PRUNE
          case (true, _) => Evaluation.EXCLUDE_AND_CONTINUE
        }
    }

    traverser.traverse(start.content)
    nodes map { node => Node.wrapNeoNode(node, db, Commit) }
  }
}