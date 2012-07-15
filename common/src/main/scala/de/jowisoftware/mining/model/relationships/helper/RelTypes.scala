package de.jowisoftware.mining.model.relationships.helper

import org.neo4j.graphdb.RelationshipType

object RelTypes {
  case class ScalaRelationshipType(val name: String) extends RelationshipType

  val contains = ScalaRelationshipType("contains")
  val containsFiles = ScalaRelationshipType("contains_files")

  val inVersion = ScalaRelationshipType("in_version")
  val fixedInVersion = ScalaRelationshipType("fixed_in_version")
  val targets = ScalaRelationshipType("targets")

  val inComponent = ScalaRelationshipType("in_component")
  val hasType = ScalaRelationshipType("has_type")
  val inMilestone = ScalaRelationshipType("in_milestone")
  val hasStatus = ScalaRelationshipType("has_status")
  val fromPerson = ScalaRelationshipType("from_person")
  val changedFile = ScalaRelationshipType("changed_file")
  val changedTicket = ScalaRelationshipType("changed_ticket")
  val hasResolution = ScalaRelationshipType("has_resolution")
  val hasSeverity = ScalaRelationshipType("has_severity")
  val hasReproducability = ScalaRelationshipType("has_reproducability")
  val hasPriority = ScalaRelationshipType("has_priority")

  val owns = ScalaRelationshipType("owns")
  val created = ScalaRelationshipType("created")
  val reported = ScalaRelationshipType("reported")
  val sponsors = ScalaRelationshipType("sponsors")

  val hasTag = ScalaRelationshipType("has_tag")
  val hasComment = ScalaRelationshipType("has_comment")

  val updates = ScalaRelationshipType("updates")
  val references = ScalaRelationshipType("references")

  val childOf = ScalaRelationshipType("child_of")
  val links = ScalaRelationshipType("links")
}