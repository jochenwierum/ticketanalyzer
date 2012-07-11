package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink

import scala.util.matching.Regex

private[trac] object SvnScmScanner {
  private val singleRegexes = List("""r(\d+|[0-9a-fA-F]{1,32})""".r,
    """\[(\d+|[0-9a-fA-F]{1,32})(?:(/[^\]]+))?\]""".r,
    """changeset:(\d+|[0-9a-fA-F]{1,32})(/[A-Za-z0-9/]+)?""".r)

  private val rangeRegexes = List(new Regex("""r(\d+:\d+)""", "revs"),
    new Regex("""\[((?:\d+|[0-9a-fA-F]{1,32}):(?:\d+|[0-9a-fA-F]{1,32}))(/[^\]]+)?\]""", "revs", "path"),
    new Regex("""log:([^\s]+)?@((?:\d+|[0-9a-fA-F]{1,32}):(?:\d+|[0-9a-fA-F]{1,32}))""", "path", "revs"))

  private def isNumeric(s: String) = s.matches("""^\d+$""")
}

private[trac] class SvnScmScanner extends ScmScanner {
  import SvnScmScanner._

  def scan(text: String): Set[ScmLink] = {
    val singleMatches = singleRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      if (theMatch.groupCount > 1 && theMatch.group(2) != null)
        ScmLink(theMatch.group(1), path = Some(theMatch.group(2)))
      else
        ScmLink(theMatch.group(1))
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

  private def makeRange(rangeSpec: String, path: Option[String]): Seq[ScmLink] = {
    val rangeParts = rangeSpec.split(":")
    for (i <- rangeParts(0).toInt to rangeParts(1).toInt) yield ScmLink(i.toString, path = path)
  }
}