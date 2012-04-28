import sbt._
import Keys._
import scala.collection.immutable.TreeSet

object TicketAnalyzerBuild extends Build {
  lazy val buildSettings = Seq(
    name := "ticketanalyzer",
    version := "1.0",
    scalaVersion := "2.9.1",
    publish := false
    )

  val aggregatedProjects: Seq[ProjectReference] = Seq(common, core, importerSvn, importerTrac, linkerTrac)
  lazy val root: Project = {
    val subDependencies = TaskKey[Seq[Classpath]]("sub-dependencies")
    val copyDependencies = TaskKey[Unit]("copy-dependencies")

	  Project(
      id = "ticketanalyzer",
      base = file("."),
      settings = Defaults.defaultSettings ++ Seq(
        subDependencies <<= aggregatedProjects.map(managedClasspath in Compile in _).join,
        copyDependencies <<= (subDependencies, target).map{ (deps, target) =>
          var dest = target / "dist" / "lib"
          dest.mkdirs()
          val fileSet = new TreeSet[File]()(new Ordering[File] {
            def compare(x: File, y: File) = x.getName.compareTo(y.getName)
          }) ++ deps.flatten.map{_.data}
          val filemap = fileSet.map{file => (file, dest / file.getName)}
          IO.copy(filemap)
        }
      )
    ) aggregate(aggregatedProjects: _*)
  }

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
