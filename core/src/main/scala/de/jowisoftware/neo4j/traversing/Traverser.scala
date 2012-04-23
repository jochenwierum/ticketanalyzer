package de.jowisoftware.neo4j.traversing

import org.neo4j.graphdb.traversal.{Evaluation => NeoEvaluation}
import org.neo4j.graphdb.traversal.{Evaluator => NeoEvaluator}
import org.neo4j.graphdb.Path
import org.neo4j.helpers.{Predicate => NeoPredicate}
import org.neo4j.kernel.Traversal

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
}