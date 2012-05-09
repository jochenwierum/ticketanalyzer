package de.jowisoftware.mining.importer
import java.util.Date
import scala.reflect.Field

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