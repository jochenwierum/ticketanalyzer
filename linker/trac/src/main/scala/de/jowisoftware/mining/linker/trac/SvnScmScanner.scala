package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink

class SvnScmScanner extends ScmScanner {
  private val singleRegexes = List("""r(\d+)""".r,
    """\[(\d+)(?:/([^\]]+))\]""".r,
    """changeset:(\d+)""".r)
  private val rangeRegexes = List("""r(\d+:\d+)""".r,
    """\[(\d+:\d+)(?:/([^\]+)])\]""".r,
    """log:([^\s]*)@(\d:\d)""".r)

  def scan(text: String) = {
    val singleMatches = singleRegexes.flatMap(_.findAllIn(text).matchData.map { theMatch =>
      if (theMatch.groupCount == 3)
        ScmLink(theMatch.group(1), false, Some(theMatch.group(2)))
      else
        ScmLink(theMatch.group(1))
    })

    val rangeMatches = rangeRegexes.flatMap(_.findAllIn(text).matchData.flatMap { theMatch =>
      val path = if (theMatch.groupCount == 3) Some(theMatch.group(2)) else None
      makeRange(theMatch.group(1), path)
    })

    singleMatches ++ rangeMatches
  }

  def makeRange(rangeSpec: String, path: Option[String]) = {
    val rangeParts = rangeSpec.split(":")
    for (i <- rangeParts(0).toInt to rangeParts(1).toInt) yield ScmLink(i.toString, false, path)
  }
}