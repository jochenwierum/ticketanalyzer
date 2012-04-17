name := "ticketanalyzer-shell"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration" % "test",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test",
	"org.neo4j" % "neo4j" % "1.7.M01" withSources,
	"org.neo4j" % "neo4j-cypher" % "1.7.M01" withSources
)

mainClass := Some("de.jowisoftware.mining.shell.Main")

retrieveManaged := true
