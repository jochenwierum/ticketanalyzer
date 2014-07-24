package de.jowisoftware.mining.importer.git

import java.io.File
import java.util.Date

import de.jowisoftware.mining.importer.CommitDataFields._
import de.jowisoftware.mining.importer.git.walker.DiffWalker
import de.jowisoftware.mining.importer.{CommitData, ImportEvents, Importer}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{Constants, Repository, RepositoryBuilder}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.TreeFilter

import scala.Array.canBuildFrom
import scala.collection.JavaConversions.iterableAsScalaIterable

class GitImporter extends Importer {
  def userOptions = new GitOptions

  def importAll(config: Map[String, String], events: ImportEvents): Unit = {
    val repository = new RepositoryBuilder().setGitDir(new File(config("gitdir"))).build()
    val git = new Git(repository)

    val countLogs = collectLogs(git)
    events.setupCommits(supportsAbbrev = true)
    events.countedCommits(countLogs.size)

    val commits = collectLogs(git)
    commits foreach {
      commit => importCommit(config, events, repository, commit)
    }
  }

  private def collectLogs(git: Git) = {
    val refs = git.getRepository.getAllRefs.values

    val logs = git.log
    for (ref <- refs) {
      if (ref.getName startsWith Constants.R_HEADS)
        logs.add(ref.getObjectId)
    }
    logs.call
  }

  private def importCommit(config: Map[String, String], events: ImportEvents, repository: Repository, commit: RevCommit): Unit = {
    val commitData = CommitData(commit.getName)
    val commitAuthor = commit.getAuthorIdent.getName

    commitData(author) = commitAuthor
    commitData(date) = new Date(commit.getCommitTime)
    commitData(message) = commit.getFullMessage
    commitData(parents) = commit.getParents.map(_.getName).toSeq

    val walk = new TreeWalk(repository)
    walk.addTree(commit.getTree)
    commit.getParents.foreach(parent => walk.addTree(parent.getTree))
    walk.setFilter(TreeFilter.ANY_DIFF)

    commitData(files) = DiffWalker.createList(walk)

    events.loadedCommit(config("repositoryname"), commitData)
  }
}
