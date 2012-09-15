package de.jowisoftware.mining.analyzers.workflow

trait Printable {
  protected def perCentWithLabel(a: Int, b: Int) = a+" ("+perCent(a.floatValue / b)+")"
  protected def perCent(f: Double) = (100.0 * f).formatted("%.2f %%")
}