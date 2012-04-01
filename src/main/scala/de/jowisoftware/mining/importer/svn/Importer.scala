package de.jowisoftware.mining.importer.svn
import de.jowisoftware.mining.importer.Importer
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNLogEntry
import de.jowisoftware.mining.importer.ImportEvents

class SVNImporter extends Importer {
  var url: String = _
  
  def importAll(events: ImportEvents, repositoryName: String): Unit = {
    val cm = SVNClientManager.newInstance();
    val lc = cm.getLogClient()
    val svnurl = SVNURL.parseURIDecoded(url)
    val rev0 = SVNRevision.create(0)
    
    lc.doLog(svnurl, Array[String]("."), rev0, rev0, SVNRevision.HEAD,
        false, true, Long.MaxValue, new ISVNLogEntryHandler() {
      def handleLogEntry(entry: SVNLogEntry) {
        events.loadedCommit(handle(entry, repositoryName))
      }
    })
  }
  
  def handle(entry: SVNLogEntry, repositoryName: String) = {
    var result = Map[String, Any]("repository" -> repositoryName)

    result += "author" -> entry.getAuthor()
    result += "date" -> entry.getDate()
    result += "message" -> entry.getMessage()
    result += "id" -> entry.getRevision()
    result += "pathes" -> entry.getChangedPaths()
    
    result
  }
}