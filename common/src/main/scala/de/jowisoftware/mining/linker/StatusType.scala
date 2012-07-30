package de.jowisoftware.mining.linker

object StatusType extends Enumeration {
  val ignore = Value("ignore")
  val reported = Value("reported")
  val assigned = Value("assigned")
  val inReview = Value("inReview")
  val qa = Value("qa")
}