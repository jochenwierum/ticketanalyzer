package de.jowisoftware.mining.importer

import scala.collection.immutable.Queue
import scala.collection.mutable

import CommitDataFields.{ parents, message, id, files, date, author }
import de.jowisoftware.mining.model.nodes.{ File, CommitRepository, Commit }
import de.jowisoftware.mining.model.relationships.{ Owns, Contains, ChildOf, ChangedFile }
import grizzled.slf4j.Logging

private[importer] trait CommitImportHandler extends ImportEvents with Logging { this: GeneralImportHelper =>
  /** Missing commit links in the format: repository -> (parentCommit -> childNodeIDs*) */
  private var missingCommitLinks: Map[String, mutable.Map[String, List[Long]]] = Map()
  private var supportsAbbrev = false
  private var roots: Set[Commit] = Set()

  def countedCommits(count: Long) {}

  def setupCommits(supportsAbbrev: Boolean) = this.supportsAbbrev = supportsAbbrev

  abstract override def finish() {
    info("Ranking nodes")
    roots.foreach(rankNodes)

    if (missingCommitLinks.exists(p => !p._2.isEmpty)) {
      error("There are unresolved commit parents which could not be imported:\n"+missingCommitLinks.toString)
    }

    super.finish
  }

  def loadedCommit(repositoryName: String, commitData: CommitData) = {
    val repository = getCommitRepository(repositoryName)
    if (!repository.supportsAbbrev() == supportsAbbrev)
      repository.supportsAbbrev(supportsAbbrev)

    info("Inserting commit "+commitData(id)+" with "+commitData(files).size+" files")
    val node = createCommit(commitData, repository)

    debug("Connecting parents of commit "+commitData(id)+": "+commitData(parents))
    connectParents(commitData, node, repository)

    debug("Catching up missing links to this commit...")
    connectMissingCommits(node, repository)

    debug("Commit "+commitData(id)+" finished")

    if (commitData(parents).length == 0) {
      roots += node
    }

    safePointReached
  }

  private def createCommit(commitData: CommitData, repository: CommitRepository) = {
    val commit = repository.obtainCommit(commitData(id))
    commit.date(commitData(date))
    commit.message(commitData(message))

    getPerson(commitData(author)).add(commit, Owns)

    debug("Adding files...")
    addFiles(commitData, repository, commit)

    commit
  }

  private def addFiles(commitData: CommitData, repository: CommitRepository, commit: Commit) {
    commitData(files).foreach {
      case (filename, value) =>
        val file = getFile(repository, filename)
        val relation = commit.add(file, ChangedFile)
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
        repository.files.add(file, Contains)
        file
    }

  private def connectParents(commitData: CommitData, node: Commit, repository: CommitRepository) =
    commitData(parents).foreach { parentId =>
      repository.findSingleCommit(parentId) match {
        case Some(commit) => node.add(commit, ChildOf)
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
              transaction.getNode(id, Commit).add(recentCommit, ChildOf)
            }
            map.remove(recentCommit.commitId())
        }
    }
  }

  private def rankNodes(root: Commit) {
    var todo: Queue[Commit] = Queue(root)

    while (!todo.isEmpty) {
      val (commit, tail) = todo.dequeue
      val parents = commit.parents.toList

      todo = if (commit.rank() == 0 && parents.forall(_.rank() != 0)) {
        commit.rank((0 /: parents)(_ max _.rank()) + 1)
        safePointReached

        tail ++ commit.children
      } else {
        tail
      }
    }
  }
}
