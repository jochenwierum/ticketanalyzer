package de.jowisoftware.mining.importer.svn

import scala.collection.JavaConversions.asScalaSet
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.wc._
import org.tmatesoft.svn.core._
import de.jowisoftware.mining.importer.CommitDataFields._
import de.jowisoftware.mining.importer._

class SVNImporter extends Importer {
  def userOptions = new SVNOptions

  def importAll(config: Map[String, String], events: ImportEvents): Unit = {
    require(config.contains("url"))
    require(config.contains("repositoryname"))
    require(config.contains("username"))
    require(config.contains("password"))

    val authManager = new BasicAuthenticationManager(config("username"), config("password"))
    val svnOptions = SVNWCUtil.createDefaultOptions(true)

    val cm = SVNClientManager.newInstance(svnOptions, authManager)
    val lc = cm.getLogClient()
    val svnurl = SVNURL.parseURIDecoded(config("url"))
    val rev0 = SVNRevision.create(1)

    val info = cm.getWCClient().doInfo(svnurl, SVNRevision.HEAD, SVNRevision.HEAD)
    val latestRevision = info.getCommittedRevision()

    events.countedCommits(latestRevision.getNumber())

    var tmp: List[SVNLogEntry] = Nil
    lc.doLog(svnurl, Array[String]("."), rev0, rev0, latestRevision,
      false, true, Long.MaxValue, new ISVNLogEntryHandler() {
        def handleLogEntry(entry: SVNLogEntry) {
          events.loadedCommit(config("repositoryname"), handle(entry))
        }
      })

    events.finish()
  }

  private def handle(entry: SVNLogEntry): CommitData = {
    val author = entry.getAuthor()
    val data = CommitData(entry.getRevision().toString())
    data(message) = entry.getMessage -> author
    data(CommitDataFields.author) = author -> author
    data(date) = entry.getDate -> author
    data(files) = (createFileList(entry.getChangedPaths()
        .asInstanceOf[java.util.Map[String, SVNLogEntryPath]])) -> author
    data(parents) = (if (entry.getRevision > 1) Seq((entry.getRevision - 1).toString) else Nil) -> author
    data
  }

  private def createFileList(pathMap: java.util.Map[String, SVNLogEntryPath]) =
    pathMap.entrySet().map {
      entry => (entry.getKey() -> entry.getValue().getType().toString)
    }.toMap
}
