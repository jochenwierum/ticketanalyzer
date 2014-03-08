package de.jowisoftware.mining.gui.results.nodeTablePane

import scala.Array.canBuildFrom
import org.neo4j.graphdb.{ Node, PropertyContainer, Relationship }
import de.jowisoftware.util.HTMLUtil

import scala.collection.JavaConversions._

object CellFormatter {
  def anyToCellData(obj: Any): CellData = obj match {
    case null => CellData("(null)", "(null)")
    case node: Node =>
      CellData(formatNode(node, identity),
        clearNamespaces(formatNode(node, shortenString(40))))
    case rel: Relationship =>
      CellData(formatRelationship(rel, identity),
        clearNamespaces(formatRelationship(rel, shortenString(40))))
    case _ => CellData(anyToString(obj, identity), anyToString(obj, shortenString(40)))
  }

  private def anyToString(obj: Any, formatter: String => String): String = {
    val unmasked = obj match {
      case n @ (_: Float | _: Double) =>
        n.formatted("%.4f")
      case a: Array[_] =>
        a.map(x => formatter(x.toString)).mkString(", ")
      case s: Iterable[_] =>
        s.map(x => formatter(x.toString)).mkString(", ")
      case x =>
        formatter(x.toString)
    }

    "<html><body>"+
      HTMLUtil.mask(unmasked.replace("""\""", """\\""").replace("\n", """\n"""))+
      "</body></html>"
  }

  private def clearNamespaces(text: String): String =
    text.replace("de.jowisoftware.mining.model.", "")

  private def formatNode(node: Node, formatter: String => String): String =
    "<b>Node["+node.getId+"]: "+getClassFromProperties(node)+
      "</b>"+formatProperties(node, formatter)

  private def formatRelationship(relationship: Relationship, formatter: String => String): String =
    "<b>Relationship["+relationship.getId+"]: "+getClassFromProperties(relationship)+
      "</b>"+formatProperties(relationship, formatter)

  private def getClassFromProperties(properties: PropertyContainer): String =
    properties.getProperty("_class", "(?)").toString

  private def formatProperties(properties: PropertyContainer, formatter: String => String) = {
    val lines = formatPropertyLines(properties, formatter)
    if (lines.size > 0) {
      lines.mkString("<br />\n", "<br />\n", "")
    } else {
      ""
    }
  }

  private def formatPropertyLines(properties: PropertyContainer, formatter: String => String): Seq[String] =
    properties.getPropertyKeys.toSeq.sorted.withFilter(!_.startsWith("_")).map { property =>
      property+": "+anyToString(properties.getProperty(property), formatter)
    }

  private def shortenString(length: Int)(text: String): String =
    if (text.length > length) text.substring(0, length - 3)+"..." else text
}