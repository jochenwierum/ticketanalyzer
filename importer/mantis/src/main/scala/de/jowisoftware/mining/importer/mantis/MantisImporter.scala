package de.jowisoftware.mining.importer.mantis

import de.jowisoftware.mining.importer.Importer
import scala.collection.immutable.Map
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.mining.importer.ImportEvents
import scala.xml.Elem
import scala.xml.PrettyPrinter
import org.joda.time.format.DateTimeFormat
import de.jowisoftware.mining.model.Ticket
import de.jowisoftware.mining.importer.TicketData

object MantisImporter {
  private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private def toDate(value: String) = dateFormat.parseDateTime(value).toDate()
}

class MantisImporter extends Importer {
  import MantisImporter._

  def userOptions(): UserOptions = new MantisOptions()

  def importAll(config: Map[String, String], events: ImportEvents) {
    require(config contains "url")
    require(config contains "username")
    require(config contains "password")
    require(config contains "project")
    require(config contains "repositoryname")

    val client = new SoapClient

    processAllTickets(config, client, events)
  }

  private def processAllTickets(config: Map[String, String], client: SoapClient, events: ImportEvents) =
    (receiveTickets(config, client) \ "mc_project_get_issuesResponse" \ "return" \ "item") foreach (_ match {
      case e: Elem => processTicket(e, events, config("repositoryname"))
      case _ =>
    })

  private def processTicket(item: Elem, events: ImportEvents, repository: String) {
    def subnode(name: String) = item \ name \ "name" text
    def node(name: String) = item \ name text

    val reproducibility = subnode("reproducibility")
    val build = node("build")
    val handler = subnode("handler")
    val eta = subnode("eta")
    //val fixedInVersion =
    //val sponsorship =
    //val relationships =
    //val comments = notes?

    val ticket = TicketData(repository, node("id").toInt,
      summary = node("summary"),
      description = node("description")+"\n"+node("steps_to_reproduce")+"\n"+node("additional_information"),
      creationDate = toDate(node("date_submitted")),
      updateDate = toDate(node("last_updated")),
      version = node("version"),
      status = subnode("status"),
      priority = subnode("priority"),
      reporter = subnode("reporter"),
      component = node("category"),
      resolution = subnode("resolution"),
      severity = subnode("severity"),
      environment = node("platform")+" "+node("os")+" "+node("osBuild"))

    println(new PrettyPrinter(120, 2).format(item))
  }

  private def receiveTickets(config: Map[String, String], client: SoapClient) =
    client.sendMessage(config("url"),
      <mc:mc_project_get_issues xmlns:mc="http://futureware.biz/mantisconnect" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <username xsi:type="xsd:string">{ config("username") }</username>
        <password xsi:type="xsd:string">{ config("password") }</password>
        <project_id xsi:type="xsd:integer">{ config("project").toInt }</project_id>
        <page_number xsi:type="xsd:integer">1</page_number>
        <per_page xsi:type="xsd:integer">-1</per_page>
      </mc:mc_project_get_issues>) match {
        case SoapResult(r) => r
        case SoapError(t, m) => throw new RuntimeException("Error ("+t+") while listing tickets:"+m)
      }
}