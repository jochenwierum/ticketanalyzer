package de.jowisoftware.mining.importer.svn
import scala.collection.JavaConversions.asScalaSet

import org.tmatesoft.svn.core.wc.{SVNRevision, SVNClientManager}
import org.tmatesoft.svn.core.{SVNURL, SVNLogEntryPath, SVNLogEntry, ISVNLogEntryHandler}

import de.jowisoftware.mining.importer.{Importer, ImportEvents}

class SVNImporter extends Importer {
  var url: String = _
  var repositoryName: String = _
  
  def importAll(events: ImportEvents): Unit = {
    val cm = SVNClientManager.newInstance();
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

  private def handle(entry: SVNLogEntry, repositoryName: String) = {
    var result = Map[String, Any]("repository" -> repositoryName)

    result += "author" -> entry.getAuthor()
    result += "date" -> entry.getDate()
    result += "message" -> entry.getMessage()
    result += "id" -> entry.getRevision()
    result += "pathes" -> createPathMap(entry.getChangedPaths()
        .asInstanceOf[java.util.Map[String, SVNLogEntryPath]])

    result
  }
  
  private def createPathMap(pathMap: java.util.Map[String, SVNLogEntryPath]) = {
    var result: Map[String, String] = Map()
    
    pathMap.entrySet().foreach {entry =>
      result += (entry.getKey() -> entry.getValue().getType().toString)
    }
    
    result
  }
}