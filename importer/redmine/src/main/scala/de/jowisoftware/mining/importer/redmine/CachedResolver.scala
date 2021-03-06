package de.jowisoftware.mining.importer.redmine

import scala.collection.mutable
import scala.xml.Elem
import scala.xml.Node
import de.jowisoftware.util.XMLUtils._
import scala.xml.NodeSeq
import scala.annotation.tailrec
import grizzled.slf4j.Logging

class CachedResolver extends Logging {
  private var cachedUsers: mutable.Map[Int, String] = mutable.Map()
  private var cachedProjects: mutable.Map[Int, String] = mutable.Map()
  private var cachedStatus: mutable.Map[Int, String] = mutable.Map()
  private var cachedVersions: mutable.Map[Int, String] = mutable.Map()
  private var cachedCategories: mutable.Map[Int, String] = mutable.Map()
  private var cachedPriorities: mutable.Map[Int, String] = mutable.Map()
  private var cachedTracker: mutable.Map[String, mutable.Map[Int, String]] = mutable.Map()

  private def getFromCache(cache: mutable.Map[Int, String], name: String)(id: Int) =
    cache.getOrElse(id, {
      warn("Looking up unknown entity "+id+" in Table "+name)
      "Unkown entity: "+id
    })

  def user = getFromCache(cachedUsers, "users") _
  def project = getFromCache(cachedProjects, "projects") _
  def status = getFromCache(cachedStatus, "status") _
  def version = getFromCache(cachedVersions, "version") _
  def category = getFromCache(cachedCategories, "categories") _
  def priority = getFromCache(cachedPriorities, "priority") _

  def tracker(name: String)(id: Int) = if (cachedTracker contains name) {
    cachedTracker(name).getOrElse(id, {
      warn("Looking up unknown entity "+id+" in Table tracker("+name+")")
      "Unkown entity: "+name+"/"+id
    })
  } else {
    warn("Looking up unknown entity "+name+" in Table tacker")
    "Unkown entity: "+name+"/"+id
  }

  private def addIfUncached(map: mutable.Map[Int, String], node: Node) = {
    val id = (node \ "@id").intText
    if (!(map contains id)) {
      map += id -> (node \ "@name").text
    }
  }

  def cacheUser(n: Node) = addIfUncached(cachedUsers, n)
  def cacheProject(n: Node) = addIfUncached(cachedProjects, n)
  def cacheStatus(n: Node) = addIfUncached(cachedStatus, n)
  def cacheVersion(n: Node) = addIfUncached(cachedVersions, n)
  def cacheCategory(n: Node) = addIfUncached(cachedCategories, n)
  def cachePriority(n: Node) = addIfUncached(cachedPriorities, n)

  def cacheTracker(project: String, name: String, id: Int) {
    if (!(cachedTracker contains project)) {
      cachedTracker += project -> mutable.Map[Int, String]()
    }

    cachedTracker(project) += id -> name
  }

  def dump() =
    "Users: "+cachedUsers.toString+"\n"+
      "Projects: "+cachedProjects.toString+"\n"+
      "Status: "+cachedStatus.toString+"\n"+
      "Versions: "+cachedVersions.toString+"\n"+
      "Categories: "+cachedCategories.toString+"\n"+
      "Tracker: "+cachedTracker.toString
}