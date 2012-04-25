import sbt._
import Keys._

object TicketAnalyzerBuild extends Build {
  lazy val buildSettings = Seq(
    name := "ticketanalyzer",
    version := "1.0",
    scalaVersion := "2.9.1",
    publish := false
    )

  lazy val root = Project(
    id = "ticketanalyzer",
    base = file(".")
    ) aggregate(common, core, importerSvn, importerTrac, linkerTrac)

  lazy val common = Project(
    id = "ticketanalyzer-common",
    base = file("common")
    )

  lazy val core = Project(
    id = "ticketanalyzer-core",
    base = file("core")
    ) dependsOn(common)

  lazy val importerSvn = Project(
    id = "ticketanalyzer-importer-svn",
    base = file("importer/svn")
    ) dependsOn(common)

  lazy val importerTrac = Project(
    id = "ticketanalyzer-importer-trac",
    base = file("importer/trac")
    ) dependsOn(common)

  lazy val linkerTrac = Project(
    id = "ticketanalyzer-linker-trac",
    base = file("linker/trac")
    ) dependsOn(common)
}
