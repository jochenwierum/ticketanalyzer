package de.jowisoftware.neo4j

import org.neo4j.kernel.Traversal
import org.neo4j.graphdb.TraversalPosition
import org.neo4j.graphdb.traversal.{Evaluator => NeoEvaluator, Evaluation => NeoEvaluation}
import org.neo4j.graphdb.Path
import org.neo4j.helpers.{Predicate => NeoPredicate}

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

  /*
  type StopEvaluator = TraversalPosition => Boolean
  implicit def stopEvaluator2NeoStopEvaluator(evaluator: StopEvaluator) = new NeoStopEvaluator() {
    override def isStopNode(currentPos: TraversalPosition): Boolean =
      evaluator(currentPos)
  }

  type ReturnableEvaluator = TraversalPosition => Boolean
  implicit def returnableEvaluator2NeoReturnableEvaluator(evaluator: StopEvaluator) = new NeoReturnableEvaluator() {
    override def isReturnableNode(currentPos: TraversalPosition): Boolean =
      evaluator(currentPos)
  }
  */
}