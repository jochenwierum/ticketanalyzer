package de.jowisoftware.mining.importer

import java.util.Date
import de.jowisoftware.mining.importer.TicketData.TicketField.TicketField
import scala.collection.SortedMap

object TicketData {
  object TicketField {
    class TicketField[T] private[TicketData] (val name: String, val default: T)(implicit manifest: Manifest[T]) {
      val valueClass = manifest.erasure
      override def toString = name+"["+valueClass.getSimpleName+"] = ("+default.toString+")"
    }

    private def ticketField[T](name: String, value: T)(implicit manifest: Manifest[T]) = {
      val result = new TicketField(name, value)
      fieldList = result :: fieldList
      result
    }

    private var fieldList: List[TicketField[_]] = List()
    def fields = fieldList

    val id = ticketField("id", 0)
    val summary = ticketField("summary", "")
    val description = ticketField("description", "")

    val creationDate = ticketField("creationDate", new Date)
    val updateDate = ticketField("updateDate", new Date)
    val status = ticketField("status", "")

    val version = ticketField("version", "")
    val fixedInVersion = ticketField("fixedInVersion", "")
    val targetVersion = ticketField("targetVersion", "")
    val milestone = ticketField("milestone", "")

    val tags = ticketField("tags", Seq[String]())
    val component = ticketField("component", "")

    val reporter = ticketField("reporter", "")
    val owner = ticketField("owner", "")
    val votes = ticketField("votes", 0)
    val eta = ticketField("eta", 0)
    val sponsors = ticketField("sponsors", Seq[String]())

    val ticketType = ticketField("ticketType", "")
    val resolution = ticketField("resolution", "")
    val priority = ticketField("priority", "")
    val severity = ticketField("severity", "")
    val reproducability = ticketField("reproducability", "")

    val relationships = ticketField("relationships", Seq[TicketRelationship]())

    val environment = ticketField("environment", "")
    val build = ticketField("build", "")

    val comments = ticketField("comments", Seq[Int]())
  }

  def apply(id: Int) = {
    val result = new TicketData()
    result(TicketField.id) = (id, "(system)")
    result
  }
}

class TicketData(reference: TicketData) {
  import TicketData.TicketField
  import TicketData.TicketField._

  def this() = this(null)

  private var values: Map[TicketField[_], (Any, String)] =
    TicketField.fields.map(field => (field -> (field.default, ""))).toMap
  values += creationDate -> (new Date(), "")
  values += updateDate -> (new Date(), "")

  if (reference != null)
    reference.values.foreach { case (k, v) => values += k -> v }

  def update[T](field: TicketField[T], value: (T, String)) =
    values += field -> value

  def apply[T](field: TicketField[T]): T =
    values(field)._1.asInstanceOf[T]

  override def toString = "TicketData(\n"+(SortedMap.empty(Ordering.by { k: TicketField[_] => k.name }) ++ values).map {
    case (k, v) => "  "+k.name+"="+niceTupel(v)
  }.mkString(",\n")+"\n)"

  private def niceTupel(t: (Any, String)) = {
    (t._1 match {
      case s: String if s.length > 50 => "\""+s.substring(0, 47)+"...\""
      case s: String => "\""+s+"\""
      case x => x.toString
    }).replace("\n", "\\n") + (if (t._2.isEmpty) "" else " by "+t._2)
  }
}