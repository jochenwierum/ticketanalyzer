name := "ticketanalyzer-shell"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-swing" % "2.9.1",
  "org.neo4j" % "neo4j" % "1.7.M01" withSources
)

mainClass := Some("de.jowisoftware.mining.shell.Main")

retrieveManaged := true
