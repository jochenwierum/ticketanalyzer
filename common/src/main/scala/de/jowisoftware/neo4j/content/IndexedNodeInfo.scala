package de.jowisoftware.neo4j.content

import org.neo4j.graphdb.{ Label => NeoLabel }
import org.neo4j.graphdb.DynamicLabel

object IndexedNodeInfo {
  object Labels {
    case class Label private[Labels] (name: String, val indexProperty: String) {
      def label: NeoLabel = DynamicLabel.label(name)
    }

    private var created: List[Label] = Nil

    def labels = created

    private def create(name: String, indexProperty: String = "name"): Label = {
      val label = new Label(name, indexProperty)
      created = label :: created
      label
    }

    val function = create("function", "_function")
    val resolution = create("resolution")
    val commitRepository = create("commitRepository")
    val component = create("component")
    val keyword = create("keyword")
    val milestone = create("milestone")
    val person = create("person")
    val priority = create("priority")
    val reproducability = create("reproducability")
    val severity = create("severity")
    val status = create("status")
    val tag = create("tag")
    val ticketRepository = create("ticketRepository")
    val `type` = create("type")
    val version = create("version")
  }

  def apply(label: Labels.Label): IndexedNodeInfo =
    apply(label.label, label.indexProperty)
}

case class IndexedNodeInfo private (
  label: NeoLabel,
  indexProperty: String)