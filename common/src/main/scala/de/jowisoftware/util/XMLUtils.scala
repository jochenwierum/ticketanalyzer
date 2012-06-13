package de.jowisoftware.util

import scala.xml.Node
import scala.xml.PrettyPrinter

object XMLUtils {
  class FormattableNode(item: Node) {
    def formatted = new PrettyPrinter(120, 2).format(item)
  }

  implicit def XML2FormatableXML(item: Node) = new FormattableNode(item)
}
