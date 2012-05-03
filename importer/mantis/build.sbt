name := "ticketanalyzer-importer-mantis"

version := "1.0"

scalaVersion := "2.9.1"

retrieveManaged := true

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)

packageOptions := Seq(
  Package.ManifestAttributes(
    ("Plugin-Class", "de.jowisoftware.mining.importer.mantis.MantisImporter"),
    ("Plugin-Type", "Tickets"),
    ("Plugin-Name", "Mantis")
  )
)
