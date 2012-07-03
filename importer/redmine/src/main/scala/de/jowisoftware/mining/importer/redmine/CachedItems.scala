package de.jowisoftware.mining.importer.redmine

import scala.collection.mutable
import scala.xml.Elem
import scala.xml.Node
import de.jowisoftware.util.XMLUtils._
import scala.xml.NodeSeq
import scala.annotation.tailrec

trait CachedItems { this: RedmineImporter =>
  private var cachedUsers: mutable.Map[Int, String] = mutable.Map()
  private var cachedTracker: mutable.Map[Int, String] = mutable.Map()
  private var cachedIssues: mutable.Map[Int, String] = mutable.Map()
  private var cachedProjects: mutable.Map[Int, String] = mutable.Map()
  private var cachedStatus: mutable.Map[Int, String] = mutable.Map()
  private var cachedCategories: mutable.Map[Int, String] = mutable.Map()

  private def collectNode(selector: Node => NodeSeq, converter: Node => (Int, String))(xml: Node) =
    selector(xml) map converter

  private def getIdNameTupleFromNodes(node: Node) =
    ((node \ "id" intText), (node \ "name" text))

  private def getIdNameTupleFromAttributes(node: Node) =
    ((node \ "@id" intText), (node \ "@name" text))

  def getStatus(id: Int, config: Map[String, String]) = {
    ensureFilledMap(cachedStatus, "issue_statuses.xml", config, collectNode(_ \ "issue_status", getIdNameTupleFromNodes))
    cachedStatus(id)
  }

  def getTracker(id: Int, config: Map[String, String]) = {
    ensureFilledMap(cachedTracker, "projects/"+config("project")+".xml", config,
      collectNode(_ \ "trackers" \ "tracker", getIdNameTupleFromAttributes),
      Map("include" -> "trackers,issue_categories"))

    cachedTracker(id)
  }

  def getCategory(id: Int, config: Map[String, String]) = {
    ensureFilledMap(cachedCategories, "projects/"+config("project")+"/issue_categories.xml", config,
      collectNode(_ \ "issue_category", getIdNameTupleFromNodes))
    cachedCategories(id)
  }

  def getProject(id: Int, config: Map[String, String]) = {
    ensureFilledMap(cachedProjects, "projects.xml", config, collectNode(_ \ "project", getIdNameTupleFromNodes))
    cachedProjects(id)
  }

  def getIssue(id: Int, config: Map[String, String]) = {
    ensureFilledMap(cachedTracker, "issue_statuses.xml", config, collectNode(_ \ "issue_status", getIdNameTupleFromNodes))
    cachedIssues(id)
  }

  def getUser(id: Int, config: Map[String, String]) = {
    ensureFilledMap(cachedUsers, "users.xml", config, collectNode(_ \ "user", {
      node => ((node \ "id" intText), (node \ "login" text))
    }))

    cachedUsers(id)
  }

  def ensureFilledMap(map: mutable.Map[Int, String], url: => String, config: Map[String, String],
    extractor: Elem => Seq[(Int, String)], args: Map[String, String] = Map()) =
    if (map.isEmpty) {
      recFillMap(map, url, extractor, config, args, 0)
    }

  @tailrec private def recFillMap(map: mutable.Map[Int, String], url: => String,
    extractor: Elem => Seq[(Int, String)],
    config: Map[String, String], args: Map[String, String] = Map(),
    start: Int) {

    val page = retrieveXML(url,
      args ++ Map(
        "offset" -> start.toString,
        "limit" -> "25"),
      config)

    extractor(page) foreach { entry => map += entry }

    val isPaged = (page \ "@limit").length > 0
    val total = if (isPaged) (page \ "@total_count" intText) else 0
    val newOffset = if (isPaged) ((page \ "@offset" intText) + (page \ "@limit" intText)) else 0

    if (newOffset < total) {
      recFillMap(map, url, extractor, config, args, newOffset)
    }
  }
}