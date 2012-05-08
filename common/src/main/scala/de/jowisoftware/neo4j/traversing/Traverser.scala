package de.jowisoftware.neo4j.traversing

import scala.collection.JavaConversions._
import org.neo4j.graphdb.traversal.{ Evaluator => NeoEvaluator, Evaluation => NeoEvaluation }
import org.neo4j.graphdb.{ RelationshipExpander => NeoRelationshipExpander, Path, Node }
import org.neo4j.helpers.{ Predicate => NeoPredicate }
import org.neo4j.kernel.Traversal
import org.neo4j.graphdb.Relationship

object Traverser {
  def apply() = Traversal.description()

  type Evaluation = Path => NeoEvaluation
  implicit def evaluator2NeoEvaluator(evaluator: Evaluation) = new NeoEvaluator {
    override def evaluate(path: Path): NeoEvaluation = evaluator(path)
  }

  type Predicate[T] = T => Boolean
  implicit def predicate[T](p: Predicate[T]): NeoPredicate[T] = new NeoPredicate[T] {
    override def accept(item: T) = p(item)
  }

  type NonReversableRelationshipExpander = Node => Seq[Relationship]
  implicit def relationshipExpander(e: NonReversableRelationshipExpander): NeoRelationshipExpander =
    relationshipExpander((e, e))

  type RelationshipExpander = (Node => Seq[Relationship], Node => Seq[Relationship])
  implicit def relationshipExpander(e: RelationshipExpander): NeoRelationshipExpander = new NeoRelationshipExpander { that =>
    def expand(n: Node) = asJavaIterable(e._1(n))
    def reversed() = new NeoRelationshipExpander {
      def expand(n: Node) = asJavaIterable(e._2(n).reverse)
      def reversed() = that
    }
  }
}