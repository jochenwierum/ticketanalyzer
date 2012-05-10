package de.jowisoftware.mining.importer
import java.util.Date
import scala.reflect.Field

object TicketData {
  private val dataTypes = Map(
    "summary" -> classOf[String],
    "description" -> classOf[String],
    "creationDate" -> classOf[Date],
    "updateDate" -> classOf[Date],
    "tags" -> classOf[String],
    "reporter" -> classOf[String],
    "version" -> classOf[String],
    "ticketType" -> classOf[String],
    "milestone" -> classOf[String],
    "component" -> classOf[String],
    "status" -> classOf[String],
    "owner" -> classOf[String],
    "resolution" -> classOf[String],
    "blocking" -> classOf[String],
    "priority" -> classOf[String],
    "blocks" -> classOf[String],
    "depends" -> classOf[String],
    "environment" -> classOf[String],
    "severity" -> classOf[String],
    "fixedInVersion" -> classOf[String],
    "votes" -> classOf[java.lang.Integer],
    "comments" -> classOf[Seq[TicketComment]],
    "updates" -> classOf[Seq[TicketUpdate]])

  private val defaults = Map(
    "summary" -> "",
    "description" -> "",
    //"creationDate" -> new Date(),
    //"updateDate" -> new Date(),
    "tags" -> "",
    "reporter" -> "",
    "version" -> "",
    "ticketType" -> "",
    "milestone" -> "",
    "component" -> "",
    "status" -> "",
    "owner" -> "",
    "resolution" -> "",
    "blocking" -> "",
    "priority" -> "",
    "blocks" -> "",
    "depends" -> "",
    "environment" -> "",
    "severity" -> "",
    "fixedInVersion" -> "",
    "votes" -> 0,
    "comments" -> List(),
    "updates" -> List())

  def apply(data: Map[String, Any]) {
    val unknownKeys = data.keys.toBuffer - dataTypes.keys.toSeq
    if (!unknownKeys.isEmpty) {
      sys.error("Illegal key(s): "+unknownKeys)
    }

    val fullData = defaults ++ Map(
      "creationDate" -> new Date(),
      "updateDate" -> new Date()) ++ data
  }
}

class TicketData(
    val repository: String,
    val id: Int,
    val summary: String = "",
    val description: String = "",
    val creationDate: Date = new Date(),
    val updateDate: Date = new Date(),
    val tags: String = "",
    val reporter: String = "",
    val version: String = "",
    val ticketType: String = "",
    val milestone: String = "",
    val component: String = "",
    val status: String = "",
    val owner: String = "",
    val resolution: String = "",
    val blocking: String = "",
    val priority: String = "",
    val blocks: String = "",
    val depends: String = "",
    val environment: String = "",
    val severity: String = "",
    val fixedInVersion: String = "",
    val votes: Int = 0,
    val comments: Seq[TicketComment] = List(),
    val updates: Seq[TicketUpdate] = List()) {

  override def toString: String = {
    val sb = new StringBuilder("TicketData(\n")

    for {
      method <- this.getClass.getDeclaredMethods.sortWith((a, b) => a.getName < b.getName)
      if (method.getParameterTypes.length == 0 && method.getName != "toString" &&
        !method.getName.startsWith("init$"))
    } {
      sb append "  " append method.getName append "=" append method.invoke(this).toString append "\n"
    }

    sb.append("  )").toString
  }
}