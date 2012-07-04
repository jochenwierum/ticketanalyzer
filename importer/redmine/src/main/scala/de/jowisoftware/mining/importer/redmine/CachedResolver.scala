package de.jowisoftware.mining.importer.redmine

import scala.collection.mutable
import scala.xml.Elem
import scala.xml.Node
import de.jowisoftware.util.XMLUtils._
import scala.xml.NodeSeq
import scala.annotation.tailrec

class CachedResolver(client: RedmineClient) {
  private var cachedUsers: mutable.Map[Int, String] = mutable.Map()
  private var cachedIssues: mutable.Map[Int, String] = mutable.Map()
  private var cachedProjects: mutable.Map[Int, String] = mutable.Map()
  private var cachedStatus: mutable.Map[Int, String] = mutable.Map()
  private var cachedVersions: mutable.Map[Int, String] = mutable.Map()
  private var cachedCategories: mutable.Map[Int, String] = mutable.Map()
  private var cachedTracker: mutable.Map[String, mutable.Map[Int, String]] = mutable.Map()

  private def collectNode(selector: Node => NodeSeq, converter: Node => (Int, String))(xml: Node) =
    selector(xml) map converter

  private def getIdNameTupleFromNodes(node: Node) =
    ((node \ "id" intText), (node \ "name" text))

  private def getIdNameTupleFromAttributes(node: Node) =
    ((node \ "@id" intText), (node \ "@name" text))

  def getStatus(id: Int) =
    lazyGet(cachedStatus, "issue_statuses.xml",
      collectNode(_ \ "issue_status", getIdNameTupleFromNodes), id)

  def getTracker(project: String)(id: Int) =
    ensureFilledSublevelMap(cachedTracker, project, "projects/"+project+".xml",
      collectNode(_ \ "trackers" \ "tracker", getIdNameTupleFromAttributes), id,
      Map("include" -> "trackers,issue_categories"))

  def getVersion(id: Int) =
    lazyGet(cachedVersions, "versions/"+id+".xml",
      collectNode(identity, getIdNameTupleFromNodes), id)

  def getCategory(id: Int) =
    lazyGet(cachedCategories, "issue_categories/"+id+".xml",
      collectNode(identity, getIdNameTupleFromNodes), id)

  def getProject(id: Int) =
    lazyGet(cachedProjects, "projects.xml",
      collectNode(_ \ "project", getIdNameTupleFromNodes), id)

  def getIssue(id: Int) =
    lazyGet(cachedIssues, "issue_statuses.xml",
      collectNode(_ \ "issue_status", getIdNameTupleFromNodes), id)

  def getUser(id: Int) =
    lazyGet(cachedUsers, "users.xml", collectNode(_ \ "user", {
      node => ((node \ "id" intText), (node \ "login" text))
    }), id)

  def ensureFilledSublevelMap(map: mutable.Map[String, mutable.Map[Int, String]], subMapName: String,
    file: => String, extractor: Elem => Seq[(Int, String)], key: Int,
    args: Map[String, String] = Map()) = {

    if (!map.contains(subMapName)) {
      val subMap = mutable.Map[Int, String]()
      map += subMapName -> subMap
    }

    lazyGet(map(subMapName), file, extractor, key, args)
  }

  def lazyGet(map: mutable.Map[Int, String], file: => String,
    extractor: Elem => Seq[(Int, String)], key: Int,
    args: Map[String, String] = Map()) = {

    if (!map.contains(key)) {
      client.retrivePagedXML(file, args, {
        page => extractor(page) foreach { entry => map += entry }
      })
    }
    map(key)
  }
}