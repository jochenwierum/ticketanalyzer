package de.jowisoftware.mining.importer
import java.util.Date

case class CommitData(repository: String, id: String,
  author: String="", message: String="", date: Date = new Date(),
  files: Map[String, String]=Map())