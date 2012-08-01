package de.jowisoftware.mining.linker.keywords

import java.io.StringReader
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.de.GermanAnalyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.Version
import de.jowisoftware.mining.linker.Linker
import org.apache.lucene.queryParser.QueryParser
import de.jowisoftware.neo4j.content.IndexAccess
import org.apache.lucene.search.TermQuery

class LuceneKeywordExtractor(lang: String) {
  private val analyzer = createAnalyzer(lang)
  private val parser = new QueryParser(Version.LUCENE_35, "", analyzer)

  def getKeyword(word: String): String =
    parser.parse(IndexAccess.mask(word)).toString

  private def createAnalyzer(lang: String) =
    lang match {
      case "de" => new GermanAnalyzer(Version.LUCENE_35)
      case "en" => new EnglishAnalyzer(Version.LUCENE_35)
      case unknown => sys.error("Unknown language: "+unknown)
    }

}
