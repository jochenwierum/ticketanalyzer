package de.jowisoftware.mining.importer
import java.util.Date

case class CommitData(id: String,
  author: String = "", message: String = "", date: Date = new Date(),
  files: Map[String, String] = Map(), parents: Seq[String] = Seq())