package de.jowisoftware.mining.importer.redmine

import scala.collection.mutable
import scala.xml.Elem
import scala.xml.Node
import de.jowisoftware.util.XMLUtils._
import scala.xml.NodeSeq

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
    fillMap(cachedStatus, "issue_statuses.xml", config, collectNode(_ \ "issue_status", getIdNameTupleFromNodes))
    cachedStatus(id)
  }

  def getTracker(id: Int, config: Map[String, String]) = {
    fillMap(cachedTracker, "projects/"+config("project")+".xml", config,
      collectNode(_ \ "trackers" \ "tracker", getIdNameTupleFromAttributes),
      Map("include" -> "trackers,issue_categories"))

    cachedTracker(id)
  }

  def getCategory(id: Int, config: Map[String, String]) = {
    fillMap(cachedCategories, "projects/"+config("project")+"/issue_categories.xml", config,
        collectNode(_ \ "issue_category" , getIdNameTupleFromNodes))
    cachedCategories(id)
  }

  def getProject(id: Int, config: Map[String, String]) = {
    fillMap(cachedProjects, "projects.xml", config, collectNode(_ \ "project", getIdNameTupleFromNodes))
    cachedProjects(id)
  }

  def getIssue(id: Int, config: Map[String, String]) = {
    fillMap(cachedTracker, "issue_statuses.xml", config, collectNode(_ \ "issue_status", getIdNameTupleFromNodes))
    cachedIssues(id)
  }

  def getUser(id: Int, config: Map[String, String]) = {
    fillMap(cachedUsers, "users.xml", config, collectNode(_ \ "user", {
      node => ((node \ "id" intText), (node \ "login" text))
    }))

    cachedUsers(id)
  }

  def fillMap(map: mutable.Map[Int, String], url: => String, config: Map[String, String],
      extractor: Elem => Seq[(Int, String)], args: Map[String, String] = Map()) =
    if (map.isEmpty) {
      val xml = retrieveXML(url, args, config)
      println(xml.formatted)
      extractor(xml) foreach { entry => map += entry }
    }
}