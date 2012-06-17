package de.jowisoftware.mining.importer.git

import java.io.File
import java.util.Date
import scala.Array.canBuildFrom
import scala.collection.JavaConversions.iterableAsScalaIterable
import org.eclipse.jgit.api.Git
import de.jowisoftware.mining.importer.CommitDataFields._
import de.jowisoftware.mining.importer.{ Importer, ImportEvents, CommitData }
import org.eclipse.jgit.lib.IndexDiff
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.treewalk.filter.TreeFilter
import de.jowisoftware.mining.importer.git.walker.DiffWalker

class GitImporter extends Importer {
  def userOptions = new GitOptions

  def importAll(config: Map[String, String], events: ImportEvents) {
    val repository = new RepositoryBuilder().setGitDir(new File(config("gitdir"))).build()
    val git = new Git(repository)

    val commits = git.log.call
    commits take 20 foreach { commit =>
      val commitData = CommitData(commit.getName)
      val commitAuthor = commit.getAuthorIdent.getName

      commitData(author) = commitAuthor -> commitAuthor
      commitData(date) = new Date(commit.getCommitTime) -> commitAuthor
      commitData(message) = commit.getFullMessage -> commitAuthor
      commitData(parents) = commit.getParents.map(_.getName).toSeq -> commitAuthor

      val walk = new TreeWalk(repository)
      walk.addTree(commit.getTree)
      commit.getParents.foreach(parent => walk.addTree(parent.getTree))
      walk.setFilter(TreeFilter.ANY_DIFF)

      commitData(files) = DiffWalker.createList(walk) -> commitAuthor

      println(commitData)
    }
  }
}