package de.jowisoftware.mining.importer

import de.jowisoftware.mining.model._
import CommitDataFields._
import java.util.Date
import scala.collection.mutable
import grizzled.slf4j.Logging

private[importer] trait CommitImportHandler extends ImportEvents with Logging { this: GeneralImportHelper =>
  /** Missing commit links in the format: repository -> commitId -> nodeId */
  private var missingCommitLinks: Map[String, mutable.Map[Int, List[Long]]] = Map()

  abstract override def finish() {
    if (missingCommitLinks.exists(p => !p._2.isEmpty)) {
      error("There are unresolved commit parents which could not be imported:\n"+missingCommitLinks.toString)
    }

    super.finish
  }

  def countedCommits(count: Long) {}

  def loadedCommit(repositoryName: String, commitData: CommitData) = {
    val repository = getCommitRepository(repositoryName)

    info("Inserting commit "+commitData(id))
    createCommit(commitData, repository)
  }

  private def createCommit(commitData: CommitData, repository: CommitRepository): Unit = {
    val commit = repository.createCommit()
    commit.commitId(commitData(id))
    commit.date(commitData(date))
    commit.message(commitData(message))

    commit.add(getPerson(commitData(author)))(Owns)

    commitData(files).foreach {
      case (filename, value) =>
        val file = getFile(repository, filename)
        val relation = commit.add(file)(ChangedFile)
        relation.editType(value)
    }

    repository.add(commit)(Contains)
  }

  private def getFile(repository: CommitRepository, name: String): File =
    repository.findFile(name) match {
      case Some(file) => file
      case None =>
        val file = repository.createFile()
        file.name(name)
        repository.add(file)(Contains)
        file
    }
}