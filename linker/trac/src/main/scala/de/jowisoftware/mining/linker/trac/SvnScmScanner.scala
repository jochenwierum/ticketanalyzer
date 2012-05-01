package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink

import scala.util.matching.Regex

class SvnScmScanner extends ScmScanner {
  private val singleRegexes = List("""r(\d+)""".r,
    """\[(\d+)(?:(/[^\]]+))?\]""".r,
    """changeset:(\d+)(/[A-Za-z0-9/]+)?""".r)

  private val rangeRegexes = List(new Regex("""r(\d+:\d+)""", "revs"),
    new Regex("""\[(\d+:\d+)(/[^\]]+)?\]""", "revs", "path"),
    new Regex("""log:([^\s]+)?@(\d+:\d+)""", "path", "revs"))

  def scan(text: String) = {
    val singleMatches = singleRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      if (theMatch.groupCount > 1 && theMatch.group(2) != null)
        ScmLink(theMatch.group(1), false, Some(theMatch.group(2)))
      else
        ScmLink(theMatch.group(1))
    }).toSet

    val rangeMatches = rangeRegexes.flatMap(_.findAllIn(text).matchData.flatMap { theMatch =>
      val path = if (theMatch.groupCount == 2 && theMatch.group("path") != null) Some(theMatch.group("path")) else None
      makeRange(theMatch.group("revs"), path)
    }).toSet

    singleMatches ++ rangeMatches
  }

  def makeRange(rangeSpec: String, path: Option[String]) = {
    val rangeParts = rangeSpec.split(":")
    for (i <- rangeParts(0).toInt to rangeParts(1).toInt) yield ScmLink(i.toString, false, path)
  }
}