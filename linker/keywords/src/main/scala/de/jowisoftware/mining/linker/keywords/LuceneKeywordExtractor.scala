package de.jowisoftware.mining.linker.keywords

import org.apache.lucene.analysis.de.GermanAnalyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.util.Version

object LuceneKeywordExtractor {
  private def mask(s: String): String =
    s.replaceAll("""([-+!(){}\[\]^"~*?:\\]|&&|\|\|)""", """\$1""")
}

class LuceneKeywordExtractor(lang: String) {
  private val analyzer = createAnalyzer(lang)
  private val parser = new QueryParser(Version.LUCENE_35, "", analyzer)

  def getKeyword(word: String): String =
    parser.parse(LuceneKeywordExtractor.mask(word)).toString

  private def createAnalyzer(lang: String) =
    lang match {
      case "de" => new GermanAnalyzer(Version.LUCENE_35)
      case "en" => new EnglishAnalyzer(Version.LUCENE_35)
      case unknown => sys.error("Unknown language: "+unknown)
    }

}
