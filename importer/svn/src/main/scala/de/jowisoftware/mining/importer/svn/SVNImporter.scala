package de.jowisoftware.mining.importer.svn

import scala.Option.option2Iterable
import scala.annotation.tailrec
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.mutable

import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.wc.{SVNWCUtil, SVNRevision, SVNClientManager}
import org.tmatesoft.svn.core.{SVNURL, SVNLogEntryPath, SVNLogEntry, ISVNLogEntryHandler}

import de.jowisoftware.mining.importer.CommitDataFields.{parents, message, files, date}
import de.jowisoftware.mining.importer.{Importer, ImportEvents, CommitDataFields, CommitData}

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

    val svnurl = SVNURL.parseURIDecoded(if(config("url") contains "@")
      config("url").substring(0, config("url").indexOf("@"))
      else config("url"))
    val rev0 = SVNRevision.create(1)
    val pegRevision = if (config("url") contains "@") {
      SVNRevision.create(config("url").substring(config("url").indexOf('@') + 1).toLong)
    } else {
      SVNRevision.HEAD
    }

    val info = cm.getWCClient().doInfo(svnurl, pegRevision, pegRevision)
    val latestRevision = info.getCommittedRevision()

    events.countedCommits(latestRevision.getNumber())

    val parents = mutable.Map[String, Long]()
    lc.doLog(svnurl, Array[String]("."), pegRevision, latestRevision, SVNRevision.create(1),
      false, true, -1, new ISVNLogEntryHandler() {
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
