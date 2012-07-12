package de.jowisoftware.mining.importer.svn

import scala.collection.JavaConversions.asScalaSet
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.wc._
import org.tmatesoft.svn.core._
import de.jowisoftware.mining.importer.CommitDataFields._
import de.jowisoftware.mining.importer._
import scala.collection.mutable
import scala.collection.JavaConversions._
import scala.annotation.tailrec

class SVNImporter extends Importer {
  def userOptions = new SVNOptions

  def importAll(config: Map[String, String], events: ImportEvents): Unit = {
    require(config.contains("url"))
    require(config.contains("repositoryname"))
    require(config.contains("username"))
    require(config.contains("password"))

    events.setupCommits(false)

    val authManager = new BasicAuthenticationManager(config("username"), config("password"))
    val svnOptions = SVNWCUtil.createDefaultOptions(true)

    val cm = SVNClientManager.newInstance(svnOptions, authManager)
    val lc = cm.getLogClient()
    val svnurl = SVNURL.parseURIDecoded(config("url"))
    val rev0 = SVNRevision.create(1)
    val parents = mutable.Map[String, Long]()

    val info = cm.getWCClient().doInfo(svnurl, SVNRevision.HEAD, SVNRevision.HEAD)
    val latestRevision = info.getCommittedRevision()

    events.countedCommits(latestRevision.getNumber())

    var tmp: List[SVNLogEntry] = Nil
    lc.doLog(svnurl, Array[String]("."), rev0, rev0, latestRevision,
      false, true, Long.MaxValue, new ISVNLogEntryHandler() {
        def handleLogEntry(entry: SVNLogEntry) {
          val commitData = handle(entry, parents)
          events.loadedCommit(config("repositoryname"), commitData)
        }
      })

    events.finish()
  }

  private def handle(entry: SVNLogEntry, parentCommitMap: mutable.Map[String, Long]): CommitData = {
    val author = entry.getAuthor()
    val data = CommitData(entry.getRevision().toString())
    data(message) = entry.getMessage
    data(CommitDataFields.author) = author
    data(date) = entry.getDate

    val changes = entry.getChangedPaths().asInstanceOf[java.util.Map[String, SVNLogEntryPath]].toMap
    val splitChanges = changes.map { case (file, state) => splitPath(file) -> state }

    data(files) = createFileList(splitChanges)

    val usedParents = findParents(splitChanges.keySet)
    val parentCommits = usedParents.flatMap(parentCommitMap.get).map(_.toString)
    usedParents.foreach(path => parentCommitMap.put(path, entry.getRevision))

    data(parents) = parentCommits.toList
    data
  }

  private def findParents(files: Set[(String, String, String)]) =
    Set[String]() ++ files.map { file => file._1+"-"+file._2 }

  private def createFileList(pathMap: Map[(String, String, String), SVNLogEntryPath]) =
    pathMap.map {
      entry => entry._1._3 -> entry._2.getType.toString
    }.toMap

  private def splitPath(fileName: String) = {
    val segs = fileName.split("/").toList

    @tailrec def findName(names: List[String]): (String, String, String) = names match {
      case "trunk" :: tail => ("trunk", "", tail.mkString("/"))
      case "tags" :: name :: tail => ("tag", name, tail.mkString("/"))
      case "branches" :: name :: tail => ("branch", name, tail.mkString("/"))
      case Nil => ("", "", fileName)
      case unknown :: tail => findName(tail)
    }

    findName(segs)
  }
}
