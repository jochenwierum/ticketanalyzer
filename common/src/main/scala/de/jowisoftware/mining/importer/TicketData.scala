package de.jowisoftware.mining.importer

import java.util.Date
import scala.reflect.Field

object TicketData {
  object TicketField {
    class TicketField[T] private[TicketData] (val name: String, val default: T)(implicit manifest: Manifest[T]) {
      val valueClass = manifest.erasure

      override def toString = name+"["+valueClass.getSimpleName+"]"
    }

    val summary = new TicketField("summary", "")
    val description = new TicketField("description", "")
    val creationDate = new TicketField("creationDate", new Date)
    val updateDate = new TicketField("updateDate", new Date)
    val tags = new TicketField("tags", "")
    val reporter = new TicketField("reporter", "")
    val version = new TicketField("version", "")
    val ticketType = new TicketField("ticketType", "")
    val milestone = new TicketField("milestone", "")
    val component = new TicketField("component", "")
    val status = new TicketField("status", "")
    val owner = new TicketField("owner", "")
    val resolution = new TicketField("resolution", "")
    val blocking = new TicketField("blocking", "")
    val priority = new TicketField("priority", "")
    val blocks = new TicketField("blocks", "")
    val depends = new TicketField("depends", "")
    val environment = new TicketField("environment", "")
    val severity = new TicketField("severity", "")
    val fixedInVersion = new TicketField("fixedInVersion", "")
    val votes = new TicketField("votes", 0)
    val comments = new TicketField("comments", Seq[TicketComment]())
    val updates = new TicketField("updates", Seq[TicketUpdate]())
    val reproducability = new TicketField("reproducability", "")

    val repository = new TicketField("repository", "")
    val id = new TicketField("id", 0)

    val fields = List(summary, description, creationDate, updateDate, tags,
      reporter, version, ticketType, milestone, component, status, owner,
      resolution, blocking, priority, blocks, depends, environment, severity,
      fixedInVersion, votes, comments, updates)
  }

  def apply(repository: String, id: Int) = {
    val result = new TicketData()
    result(TicketField.repository) = (repository, "(system)")
    result(TicketField.id) = (id, "(system)")
    result
  }
}

class TicketData private () {
  import TicketData.TicketField
  import TicketData.TicketField._

  private var values: Map[TicketField[_], (Any, String)] =
    TicketField.fields.map(field => (field -> (field.default, ""))).toMap
  values += creationDate -> (new Date(), "")
  values += updateDate -> (new Date(), "")

  def update[T](field: TicketField[T], value: (T, String)) =
    values += field -> value

  def apply[T](field: TicketField[T]): T =
    values(field).asInstanceOf[T]

  override def toString = "TicketData(\n"+values.map {
    case (k, v) => "  "+k+"="+niceTupel(v)
  }.mkString(",\n")+"  )"

  private def niceTupel(t: (Any, String)) = {
    (t._1 match {
      case s: String if s.length > 50 => "\""+s.substring(0, 47)+"...\""
      case s: String => "\""+s+"\""
      case x => x.toString
    }).replace("\n", "\\n") + (if (t._2.isEmpty) "" else " by "+t._2)
  }
}