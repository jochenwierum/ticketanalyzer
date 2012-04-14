package de.jowisoftware.mining.importer.svn
import scala.collection.JavaConversions.asScalaSet
import org.tmatesoft.svn.core.wc.{SVNRevision, SVNClientManager}
import org.tmatesoft.svn.core.{SVNURL, SVNLogEntryPath, SVNLogEntry, ISVNLogEntryHandler}
import de.jowisoftware.mining.importer.{Importer, ImportEvents}
import de.jowisoftware.mining.importer.CommitData

class SVNImporter extends Importer {
  var url: String = _
  var repositoryName: String = _

  def importAll(config: Map[String, String], events: ImportEvents): Unit = {
    url = config("url")
    repositoryName = config("repositoryName")

    val cm = SVNClientManager.newInstance()
    val lc = cm.getLogClient()
    val svnurl = SVNURL.parseURIDecoded(url)
    val rev0 = SVNRevision.create(1)

    val info = cm.getWCClient().doInfo(svnurl, SVNRevision.HEAD, SVNRevision.HEAD)
    val latestRevision = info.getCommittedRevision()

    events.countedCommits(latestRevision.getNumber())

    lc.doLog(svnurl, Array[String]("."), rev0, rev0, latestRevision,
        false, true, Long.MaxValue, new ISVNLogEntryHandler() {
      def handleLogEntry(entry: SVNLogEntry) {
        events.loadedCommit(handle(entry, repositoryName))
      }
    })

    events.finish()
  }

  private def handle(entry: SVNLogEntry, repositoryName: String): CommitData = {
    CommitData(
      repositoryName,
      entry.getRevision().toString(),
      message=entry.getMessage(),
      date=entry.getDate(),
      author=entry.getAuthor(),
      files=createFileList(entry.getChangedPaths()
        .asInstanceOf[java.util.Map[String, SVNLogEntryPath]]))
  }

  private def createFileList(pathMap: java.util.Map[String, SVNLogEntryPath]) = {
    var result: Map[String, String] = Map()

    pathMap.entrySet().foreach {entry =>
      result += (entry.getKey() -> entry.getValue().getType().toString)
    }

    result
  }
}