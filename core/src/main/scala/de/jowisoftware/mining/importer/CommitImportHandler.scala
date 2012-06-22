package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model.nodes._
import CommitDataFields._
import java.util.Date
import scala.collection.mutable
import grizzled.slf4j.Logging

private[importer] trait CommitImportHandler extends ImportEvents with Logging { this: GeneralImportHelper =>
  /** Missing commit links in the format: repository -> (parentCommit -> childNodeIDs*) */
  private var missingCommitLinks: Map[String, mutable.Map[String, List[Long]]] = Map()

  def countedCommits(count: Long) {}

  abstract override def finish() {
    if (missingCommitLinks.exists(p => !p._2.isEmpty)) {
      error("There are unresolved commit parents which could not be imported:\n"+missingCommitLinks.toString)
    }

    super.finish
  }

  def loadedCommit(repositoryName: String, commitData: CommitData) = {
    val repository = getCommitRepository(repositoryName)

    info("Inserting commit "+commitData(id)+" with "+commitData(files).size+" files")
    val node = createCommit(commitData, repository)

    debug("Connecting parents of commit "+commitData(id)+": "+commitData(parents))
    connectParents(commitData, node, repository)

    debug("Catching up missing links to this commit...")
    connectMissingCommits(node, repository)

    debug("Commit "+commitData(id)+" finished")

    safePointReached
  }

  private def createCommit(commitData: CommitData, repository: CommitRepository) = {
    val commit = repository.createCommit()
    commit.commitId(commitData(id))
    commit.date(commitData(date))
    commit.message(commitData(message))
    commit.uid(repository.name()+"-"+commitData(id))

    commit.add(getPerson(commitData(author)))(Owns)

    debug("Adding files...")
    addFiles(commitData, repository, commit)

    repository.add(commit)(Contains)
    commit
  }

  private def addFiles(commitData: CommitData, repository: CommitRepository, commit: Commit) {
    commitData(files).foreach {
      case (filename, value) =>
        val file = getFile(repository, filename)
        val relation = commit.add(file)(ChangedFile)
        relation.editType(value)
    }
  }

  private def getFile(repository: CommitRepository, name: String): File =
    repository.files.findFile(name) match {
      case Some(file) => file
      case None =>
        trace("Creating entry for file "+name)
        val file = repository.files.createFile()
        file.name(name)
        file.uid(repository.name()+"-"+name)
        repository.files.add(file)(Contains)
        file
    }

  private def connectParents(commitData: CommitData, node: Commit, repository: CommitRepository) =
    commitData(parents).foreach { parentId =>
      repository.findCommit(parentId) match {
        case Some(commit) => node.add(commit)(ChildOf)
        case None => addMissingLink(repository, parentId, node.id)
      }
    }

  private def addMissingLink(repository: CommitRepository, parentId: String, childNodeId: Long) {
    trace("Commit "+parentId+" of node "+childNodeId+" is not known yet - queuing operation up")
    val repositoryMap = acquireMissingCommitLinksForRepository(repository)
    repositoryMap += parentId -> (childNodeId :: repositoryMap.getOrElse(parentId, Nil))
  }

  private def acquireMissingCommitLinksForRepository(repository: CommitRepository) =
    missingCommitLinks.get(repository.name()) match {
      case Some(value) => value
      case None =>
        val map = mutable.Map[String, List[Long]]()
        missingCommitLinks += repository.name() -> map
        map
    }

  def connectMissingCommits(recentCommit: Commit, repository: CommitRepository) {
    missingCommitLinks.get(repository.name()) match {
      case None =>
      case Some(map) =>
        map.get(recentCommit.commitId()) match {
          case None =>
          case Some(list) =>
            list.foreach { id =>
              trace("Adding reference from already visited node "+id)
              transaction.getNode(id)(Commit).add(recentCommit)(ChildOf)
            }
            map.remove(recentCommit.commitId())
        }
    }
  }
}
