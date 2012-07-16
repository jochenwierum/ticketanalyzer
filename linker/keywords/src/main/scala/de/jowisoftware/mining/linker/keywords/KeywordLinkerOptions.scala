package de.jowisoftware.mining.linker.keywords

import de.jowisoftware.mining.UserOptions

class KeywordLinkerOptions extends UserOptions {
  protected var result: Map[String, String] = Map(
      "language" -> "en",
      "parseTags" -> "true",
      "parseTitle" -> "true",
      "parseText" -> "true",
      "parseComments" -> "true",
      "filterNum" -> "true",
      "filterAlphaNum" -> "true",
      "filterWordList" -> "true"
      )

  def getHtmlDescription(): String = """<b>Keyword Linker</b>"""

  def fillPanel(panel: CustomizedGridBagPanel) {
    panel.add("Stemmer language", combobox("language", Seq("de", "en")))
    panel.add("Parse tags", checkbox("parseTags", "Use tags as keywords"))
    panel.add("Parse title", checkbox("parseTitle", "Use words in title as keywords"))
    panel.add("Parse text", checkbox("parseText", "Use words in body as keywords"))
    panel.add("Parse comments", checkbox("parseComments", "Use words in comments as keywords"))
    // TODO: parse for classnames?
    panel.add("Filter numbers", checkbox("filterNum", "Filter out words like '2'"))
    panel.add("Filter alphanum.", checkbox("filterAlhaNum", "Filter out words like 'r4'"))
    panel.add("Filter by wordlist", checkbox("filterNumbers", "Filter out words in settings/keywordblacklist.txt"))
  }
}