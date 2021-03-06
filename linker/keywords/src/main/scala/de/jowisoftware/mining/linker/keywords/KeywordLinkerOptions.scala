package de.jowisoftware.mining.linker.keywords

import de.jowisoftware.mining.UserOptions
import scala.swing.Label
import scala.swing.Alignment

class KeywordLinkerOptions extends UserOptions("linker.keyword") {
  protected val defaultResult: Map[String, String] = Map(
    "language" -> "en",
    "parseTags" -> "true",
    "parseTitle" -> "true",
    "parseText" -> "true",
    "parseComments" -> "true",
    "filterUniversal" -> "true",
    "filterNum" -> "true",
    "filterAlphaNum" -> "true",
    "filterWhitelist" -> "false",
    "filterBlacklist" -> "false",
    "filterShort" -> "true",
    "filterAbbrevs" -> "true",
    "filterCamelCase" -> "true",
    "filterAccept" -> "true",
    "filterShortMinLength" -> "3")

  protected val htmlDescription = """<b>Keyword Linker</b>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Stemmer language", combobox("language", Seq("de", "en")))
    panel.addSpace()

    panel.add("Parse tags", checkbox("parseTags", "Use tags as keywords"))
    panel.add("Parse title", checkbox("parseTitle", "Use words in title as keywords"))
    panel.add("Parse text", checkbox("parseText", "Use words in body as keywords"))
    panel.add("Parse comments", checkbox("parseComments", "Use words in comments as keywords"))
    panel.addSpace()

    val sectionCaption = new Label("<html>All checked filters are applied from top to bottom<br />"+
      "The first match decides what happens with the keyword</html>")
    sectionCaption.horizontalAlignment = Alignment.Left
    panel.add("Filters", sectionCaption)
    panel.add("Use universal filter", checkbox("filterUniversal", "Apply all regexes in settings/linker-keyword-universal-*.txt"))
    panel.add("Reject short words", checkbox("filterShort", "Remove words shorter than"), text("filterShortMinLength"), label("letters"))
    panel.add("Reject numbers", checkbox("filterNum", "Remove words like '2'"))
    panel.add("Reject alphanumerical words", checkbox("filterAlphaNum", "Remove words like 'r4'"))
    panel.add("Accept uppercase abbrevs", checkbox("filterAbbrevs", "Accepts words like 'REST'"))
    panel.add("Accept CamelCase words", checkbox("filterCamelCase", "Accepts words like 'HelloWorld'"))
    panel.add("Accept by wordlist", checkbox("filterWhitelist", "Accept all words (regexes) in settings/linker-keyword-whitelist-*.txt"))
    panel.add("Reject by wordlist", checkbox("filterBlacklist", "Reject words (regexes) in settings/linker-keyword-blacklist-*.txt"))
    panel.addSpace()

    panel.add("Accept all other words", checkbox("filterAccept", "Allow the rest (otherwise, they are rejected)"))
  }
}