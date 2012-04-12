name := "ticketanalyzer-importer-svn"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Tmatesoft Maven Repository" at
  "http://maven.tmatesoft.com/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.tmatesoft.svnkit" % "svnkit" % "1.3.7"
)

retrieveManaged := true


packageOptions := Seq(
  Package.ManifestAttributes(
    ("Plugin-Class", "de.jowisoftware.mining.importer.svn.SVNImporter"),
    ("Plugin-Type", "SCM")
  )
)
