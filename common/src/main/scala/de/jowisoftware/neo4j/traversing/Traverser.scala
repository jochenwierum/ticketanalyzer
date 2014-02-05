package de.jowisoftware.neo4j.traversing

import scala.language.implicitConversions

import scala.collection.JavaConversions._
import org.neo4j.graphdb.traversal.{ Evaluator => NeoEvaluator, Evaluation => NeoEvaluation }
import org.neo4j.graphdb.{ RelationshipExpander => NeoRelationshipExpander, Path, Node }
import org.neo4j.helpers.{ Predicate => NeoPredicate }
import org.neo4j.kernel.Traversal
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.GraphDatabaseService

object Traverser {
  def apply(service: GraphDatabaseService) = service.traversalDescription()

  type Evaluation = Path => NeoEvaluation
  implicit def evaluator2NeoEvaluator(evaluator: Evaluation) = new NeoEvaluator {
    override def evaluate(path: Path): NeoEvaluation = evaluator(path)
  }

  type Predicate[T] = T => Boolean
  implicit def predicate[T](p: Predicate[T]): NeoPredicate[T] = new NeoPredicate[T] {
    override def accept(item: T) = p(item)
  }
}