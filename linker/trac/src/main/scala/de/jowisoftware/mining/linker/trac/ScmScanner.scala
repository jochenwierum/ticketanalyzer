package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink
import scala.util.matching.Regex
import de.jowisoftware.mining.model.nodes.CommitRepository
import scala.util.matching.Regex.Match
import de.jowisoftware.mining.model.nodes.Commit

private[trac] object ScmScanner {
  private val singleRegexes = List("""(?<![\w\d])r(\d{1,18}|[0-9a-fA-F]{5,32})(?=\W|$)""".r,
    """(?<![\w\d])\[(\d{1,18}|[0-9a-fA-F]{5,32})(?:(/[^\]]+))?\]""".r,
    """(?<![\w\d])(?i)changeset:([0-9a-fA-F]{5,32}|\d{1,18})(/[A-Za-z0-9/]+)?""".r)

  private val rangeRegexes = List(new Regex("""(?<![\w\d])r(\d{1,18}:\d{1,18})(?=\W|$)""", "revs"),
    new Regex("""(?<![\w\d])\[((?:[0-9a-fA-F]{1,32}|\d{1,18}):(?:[0-9a-fA-F]{5,32}|\d{1,18}))(/[^\]]+)?\]""", "revs", "path"),
    new Regex("""(?<![\w\d])(?i)log:([^\s]+)?@((?:[0-9a-fA-F]{5,32}|\d{1,18}):(?:\d{1,18}|[0-9a-fA-F]{5,32}))(?=\W|$)""", "path", "revs"))
}

private[trac] class ScmScanner(rangeGenerator: RangeGenerator) {
  import ScmScanner._

  def scan(text: String, commitRepository: CommitRepository): Set[ScmLink] = {
    val singleMatches = singleRegexes.flatMap(_.findAllIn(text).matchData.flatMap { theMatch =>
      processSingleMatch(theMatch, commitRepository)
    }).toSet

    val rangeMatches = rangeRegexes.flatMap(_.findAllIn(text).matchData.flatMap { theMatch =>
      processRangeMatch(theMatch, commitRepository)
    }).toSet

    singleMatches ++ rangeMatches
  }

  private def processRangeMatch(theMatch: Match, repository: CommitRepository): Seq[ScmLink] = {
    val path = if (theMatch.groupCount == 2)
      Option(theMatch.group("path"))
    else
      None

    makeRange(theMatch.group("revs"), path, repository)
  }

  private def processSingleMatch(theMatch: Match, repository: CommitRepository): Option[ScmLink] =
    repository.findSingleCommit(theMatch.group(1)).map { commit =>
      if (theMatch.groupCount == 2)
        ScmLink(commit.commitId(), path = Option(theMatch.group(2)))
      else
        ScmLink(commit.commitId(), path = None)
    }

  private def makeRange(rangeSpec: String, path: Option[String], repository: CommitRepository): Seq[ScmLink] = {
    val rangeParts = rangeSpec.split(":")
    val commitRange = for {
      start <- repository.findSingleCommit(rangeParts(0))
      end <- repository.findSingleCommit(rangeParts(1))
    } yield {
      rangeGenerator.findRange(start, end).map(commit => ScmLink(commit.commitId(), path = path)).toSeq
    }
    commitRange getOrElse Seq()
  }
}