package de.jowisoftware.util
import scala.collection.generic.FilterMonadic
import scala.collection.generic.CanBuildFrom

object IterationUtil {
  implicit def Iterator2NoneValueRemover[A](it: Iterator[Option[A]]): NoneValueRemover[A] =
    new NoneValueRemover(it)

  implicit def Seq2NoneValueRemover[A](seq: FilterMonadic[Option[A], A]): NoneValueRemover2[A] =
    new NoneValueRemover2(seq)

  class NoneValueRemover2[A] private[IterationUtil](seq: FilterMonadic[Option[A], A]) {
    def withoutNoneValues[Repr, That](implicit bf: CanBuildFrom[Repr, A, That]) : That = {
      val builder = bf.apply()
      seq.foreach(_ match {
        case Some(value) => builder += value
        case None =>
      })
      return builder.result()
    }
  }

  class NoneValueRemover[A] private[IterationUtil] (it: Iterator[Option[A]]) {
    def withoutNoneValues() : Iterator[A] = {
      it.filter(_ match {
        case Some(_) => true
        case None => false
      }).map(_.get)
    }
  }
}