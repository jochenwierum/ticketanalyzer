name := "ticketanalyzer-importer-trac"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)

retrieveManaged := true
