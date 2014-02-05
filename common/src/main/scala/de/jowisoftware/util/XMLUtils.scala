package de.jowisoftware.util

import scala.language.implicitConversions

import scala.xml.Node
import scala.xml.PrettyPrinter
import scala.xml.NodeSeq

object XMLUtils {
  class EnrichedNodeSeq(item: NodeSeq) {
    def intText = item.text.toInt
    def floatText = item.text.toFloat
  }

  class EnrichedNode(item: Node) {
    def formatted = new PrettyPrinter(120, 2).format(item)
  }

  implicit def Node2EnrichedNode(item: Node) = new EnrichedNode(item)
  implicit def NodeSeq2EnrichedNodeSeq(item: NodeSeq) = new EnrichedNodeSeq(item)
}
