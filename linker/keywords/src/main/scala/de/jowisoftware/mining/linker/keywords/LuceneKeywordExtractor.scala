package de.jowisoftware.lucenetest

import java.io.StringReader
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.de.GermanAnalyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.util.Version

class LuceneKeywordExtractor {
  def getKeywords(text: String): Set[String] = {
    val deText = stem(text, "de")
    val enText = stem(text, "en")

    toSet(deText) ++ toSet(enText)
  }

  private def toSet(text: String): Set[String] = {
    text.split("""\P{Alpha}+""").toSet - ""
  }

  private def stem(text: String, lang: String): String = {
    val reader = new StringReader(text)
    val analyzer = createAnalyzer(lang)
    val tokenStream = analyzer.tokenStream("contents", reader)

    convertTokenStreamToString(tokenStream)
  }

  private def createAnalyzer(lang: String) =
    lang match {
      case "de" => new GermanAnalyzer(Version.LUCENE_36)
      case "en" => new EnglishAnalyzer(Version.LUCENE_36)
      case unknown => sys.error("Unknown language: "+unknown)
    }

  private def convertTokenStreamToString(tokenStream: TokenStream): String = {
    val term = tokenStream.addAttribute(classOf[CharTermAttribute])
    val resultBuilder = StringBuilder.newBuilder

    while (tokenStream.incrementToken) {
      if (resultBuilder.length != 0) {
        resultBuilder += ' '
      }

      resultBuilder ++= term.toString
    }

    resultBuilder.toString
  }

  def main(args: Array[String]) = {
    val text = "He goes shopping every day at night"
    val text2 = "Er geht jeden Abend um Mitternacht einkaufen"
    println(stem(text, "en")) // he goe shop everi dai night
    println(stem(text2, "de")) // geht abend mitternacht einkauf
  }
}
