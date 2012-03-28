package de.jowisoftware.mining.model
import _root_.de.jowisoftware.neo4j._

object Ticket extends NodeCompanion[Ticket] {
  def apply = new Ticket
} 

class Ticket extends Node {
  val version = 1
  def updateFrom(version: Int) = {}
}

object Commit extends NodeCompanion[Commit] {
  def apply = new Commit
}

class Commit extends Node {
  val version = 1
  def updateFrom(version: Int) = {}
  
  val comment = stringProperty("comment")
  val committer = stringProperty("committer")
  val id = stringProperty("id")
}