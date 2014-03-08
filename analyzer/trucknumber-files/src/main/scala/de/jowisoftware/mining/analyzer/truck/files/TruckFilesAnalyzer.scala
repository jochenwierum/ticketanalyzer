package de.jowisoftware.mining.analyzer.truck.files

import scala.collection.SortedSet

import de.jowisoftware.mining.analyzer.{ Analyzer, AnalyzerResult, NodeResult }
import de.jowisoftware.mining.gui.ProgressMonitor
import de.jowisoftware.mining.model.nodes.{ CommitRepository, Person }
import de.jowisoftware.neo4j.{ DBWithTransaction, Database }

class TruckFilesAnalyzer extends Analyzer {
  def userOptions() = new TruckFilesAnalyzerOptions()

  def analyze(db: DBWithTransaction, options: Map[String, String],
    waitDialog: ProgressMonitor) =
    createCiriticalFilesResult(db, options)

  private def createCiriticalFilesResult(transaction: DBWithTransaction,
    options: Map[String, String]): AnalyzerResult = {
    val query = s"""
      MATCH ${CommitRepository.cypherForAll("n")}-[:contains_files]->file<-[:changed_file]-commit<-[:owns]-person
      WHERE NOT (person.name in ({ignored}))
      RETURN
        file.name as file,
        1.0 * count(distinct commit.id) / count(distinct person.name) as ratio,
        count(commit.id) as commitCount,
        count(distinct person.name) as personCount,
        collect(distinct person.name) as persons
      ORDER BY ratio DESC
      LIMIT {limit};"""

    val result = transaction.cypher(query,
      Map("limit" -> options("limit").toInt,
        "ignored" -> options("inactive").split("""\s*,\s*""").toArray))

    if (options("output") == "raw")
      new NodeResult(result, "Truck Number by files: raw result")
    else {
      val persons = getActivePersons(transaction,
        options("inactive").split("""\s*,\s*""")).toSet
      val titles = Array("File", "Ratio", "Persons with knowledge", "Persons without knowledge")
      val fields = Array("file", "ratio", "personsWithKnowledge", "missingPersons")
      new NodeResult(transformToInterpreted(result, persons), fields, titles, "Truck Number by Tickets")
    }
  }

  def transformToInterpreted(result: Iterator[Map[String, Any]], activePersons: Set[String]) = {
    val sortedActive: Set[String] = SortedSet.empty[String] ++ activePersons

    for (row <- result) yield {
      val persons = row("persons").asInstanceOf[List[String]].toSet
      val missingPersons = sortedActive -- persons
      Map("file" -> row("file").asInstanceOf[String],
        "ratio" -> row("ratio").asInstanceOf[Double].toString,
        "personsWithKnowledge" -> persons.toSeq.sorted.mkString(", "),
        "missingPersons" -> missingPersons.toSeq.mkString(", "))
    }
  }

  private def getActivePersons(transaction: DBWithTransaction, ignoredList: Array[String]): Seq[String] =
    transaction.cypher(s"""MATCH ${Person.cypherForAll("n")} WHERE NOT n.name IN ({ignored}) RETURN n.name""",
      Map("ignored" -> ignoredList.toArray[String]))
      .map(_.getOrElse("name", "").asInstanceOf[String])
      .toSeq
}