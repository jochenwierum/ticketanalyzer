package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink
import scala.util.matching.Regex
import de.jowisoftware.mining.model.nodes.CommitRepository
import scala.util.matching.Regex.Match
import de.jowisoftware.mining.model.nodes.Commit

private[trac] object ScmScanner {
  private val singleRegexes = List("""r(\d+|[0-9a-fA-F]{1,32})""".r,
    """\[(\d+|[0-9a-fA-F]{1,32})(?:(/[^\]]+))?\]""".r,
    """changeset:(\d+|[0-9a-fA-F]{1,32})(/[A-Za-z0-9/]+)?""".r)

  private val rangeRegexes = List(new Regex("""r(\d+:\d+)""", "revs"),
    new Regex("""\[((?:\d+|[0-9a-fA-F]{1,32}):(?:\d+|[0-9a-fA-F]{1,32}))(/[^\]]+)?\]""", "revs", "path"),
    new Regex("""log:([^\s]+)?@((?:\d+|[0-9a-fA-F]{1,32}):(?:\d+|[0-9a-fA-F]{1,32}))""", "path", "revs"))
}

private[trac] class ScmScanner {
  import ScmScanner._

  def scan(text: String, commitRepository: CommitRepository): Set[ScmLink] = {
    val singleMatches = singleRegexes.flatMap(_.findAllIn(text).matchData.flatMap { theMatch =>
      processSingleMatch(theMatch, commitRepository)
    }).toSet

    val rangeMatches = rangeRegexes.flatMap(_.findAllIn(text).matchData.flatMap { theMatch =>
      val path = if (theMatch.groupCount == 2 && theMatch.group("path") != null)
        Some(theMatch.group("path"))
      else
        None
      makeRange(theMatch.group("revs"), path)
    }).toSet

    singleMatches ++ rangeMatches
  }

  private def processSingleMatch(theMatch: Match, repository: CommitRepository): Option[ScmLink] =
    repository.findCommit(theMatch.group(1)).map { commit =>
      if (theMatch.groupCount == 2)
        ScmLink(commit.commitId(), path = Option(theMatch.group(2)))
      else
        ScmLink(commit.commitId(), path = None)
    }

  private def makeRange(rangeSpec: String, path: Option[String]): Seq[ScmLink] = {
    val rangeParts = rangeSpec.split(":")
    for (i <- rangeParts(0).toInt to rangeParts(1).toInt) yield ScmLink(i.toString, path = path)
  }
}