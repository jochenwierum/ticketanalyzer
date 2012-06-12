package de.jowisoftware.mining.importer.svn
import scala.collection.JavaConversions.asScalaSet
import org.tmatesoft.svn.core.wc.{ SVNRevision, SVNClientManager }
import org.tmatesoft.svn.core.{ SVNURL, SVNLogEntryPath, SVNLogEntry, ISVNLogEntryHandler }
import de.jowisoftware.mining.importer.{ Importer, ImportEvents }
import de.jowisoftware.mining.importer.CommitData
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.wc.SVNWCClient
import org.tmatesoft.svn.core.wc.SVNWCUtil

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

    lc.doLog(svnurl, Array[String]("."), rev0, rev0, latestRevision,
      false, true, Long.MaxValue, new ISVNLogEntryHandler() {
        def handleLogEntry(entry: SVNLogEntry) {
          events.loadedCommit(config("repositoryname"), handle(entry))
        }
      })

    events.finish()
  }

  private def handle(entry: SVNLogEntry): CommitData = {
    CommitData(
      entry.getRevision().toString(),
      message = entry.getMessage(),
      date = entry.getDate(),
      author = entry.getAuthor(),
      files = createFileList(entry.getChangedPaths()
        .asInstanceOf[java.util.Map[String, SVNLogEntryPath]]),
      parents = Seq((entry.getRevision - 1).toString))
  }

  private def createFileList(pathMap: java.util.Map[String, SVNLogEntryPath]) = {
    var result: Map[String, String] = Map()

    pathMap.entrySet().foreach { entry =>
      result += (entry.getKey() -> entry.getValue().getType().toString)
    }

    result
  }
}