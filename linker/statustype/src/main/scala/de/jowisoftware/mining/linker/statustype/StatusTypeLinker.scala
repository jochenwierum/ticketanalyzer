package de.jowisoftware.mining.linker.statustype

import java.io.{ FileInputStream, File }
import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.immutable.Map
import scala.util.Properties
import de.jowisoftware.mining.linker.{ StatusType, Linker, LinkEvents }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, CommitRepository }
import de.jowisoftware.mining.UserOptions
import de.jowisoftware.util.AppUtil
import grizzled.slf4j.Logging
import de.jowisoftware.mining.model.nodes.Status
import de.jowisoftware.util.AppUtil
import de.jowisoftware.util.AppUtil

class StatusTypeLinker extends Linker with Logging {
  def userOptions() = new StatusTypeOptions

  def link(tickets: TicketRepository, commits: CommitRepository, options: Map[String, String], events: LinkEvents) {
    val statusMap = readStatusProperties

    tickets.rootNode.statusCollection.children foreach { status =>
      events.foundStatusMap(status, statusMap.getOrElse(status.name().toLowerCase, warnIgnore(status)))
    }

    events.finish
  }

  private def warnIgnore(status: Status) = {
    warn("State '"+status.name().toLowerCase+"' is not mapped! Ignoring it...")
    StatusType.ignore
  }

  private def readStatusProperties(): Map[String, StatusType.Value] = {
    val properties = AppUtil.loadSettings("linker-type-statusmap.properties") getOrElse {
      sys.error("Missing file: settings/linker-type-statusmap.properties")
    }

    properties.propertyNames.map { name =>
      (name, findStatusByName(properties.getString(name)))
    }.toMap
  }

  private def findStatusByName(name: String): StatusType.Value =
    try {
      StatusType.withName(name)
    } catch {
      case e: NoSuchElementException =>
        error("Could not find state '"+name+"', please use only: "+
          StatusType.values.map(_.toString).mkString(", "))
        StatusType.ignore
    }
}