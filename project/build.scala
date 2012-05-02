import sbt._
import Keys._
import scala.collection.immutable.TreeSet

object TicketAnalyzerBuild extends Build with Projects {
  lazy val buildSettings = Seq(
    name := "ticketanalyzer",
    version := "1.0",
    scalaVersion := "2.9.1",
    publish := false
    )

  lazy val root: Project = {
    val subDependencies = TaskKey[Seq[Classpath]]("sub-dependencies")
    val copyDependencies = TaskKey[Unit]("copy-dependencies")

    Project(
      id = "ticketanalyzer",
      base = file("."),
      settings = Defaults.defaultSettings ++ Seq(
        subDependencies <<= projectList.map(managedClasspath in Compile in _).join,
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
    ) aggregate(projectList map (p => Reference.projectToRef(p)): _*)
  }
}
