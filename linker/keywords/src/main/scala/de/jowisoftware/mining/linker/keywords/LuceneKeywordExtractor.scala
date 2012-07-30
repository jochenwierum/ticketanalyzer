package de.jowisoftware.mining.linker.keywords

import java.io.StringReader
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.de.GermanAnalyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.Version
import de.jowisoftware.mining.linker.Linker

object LuceneKeywordExtractor {
  private val forbidden = Set("AND", "NOT", "OR")
}

class LuceneKeywordExtractor(lang: String) {
  private val analyzer = createAnalyzer(lang)

  def getKeywords(text: String): Set[String] = {
    val reader = new StringReader(text)
    val tokenStream = analyzer.tokenStream("contents", reader)

    convertTokenStreamToString(tokenStream)
  }

  private def createAnalyzer(lang: String) =
    lang match {
      case "de" => new GermanAnalyzer(Version.LUCENE_35)
      case "en" => new EnglishAnalyzer(Version.LUCENE_35)
      case unknown => sys.error("Unknown language: "+unknown)
    }

  private def convertTokenStreamToString(tokenStream: TokenStream): Set[String] = {
    val term = tokenStream.addAttribute(classOf[CharTermAttribute])
    val resultBuilder = StringBuilder.newBuilder

    var uniqueWords: Set[String] = Set()
    while (tokenStream.incrementToken) {
      val word = term.toString
      if (!LuceneKeywordExtractor.forbidden.contains(word.toUpperCase)) {
        uniqueWords += word
      }
    }

    uniqueWords
  }
}
