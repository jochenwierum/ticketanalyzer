name := "ticketanalyzer-linker-trac"

version := "1.0"

scalaVersion := "2.9.1"

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration" % "test",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test"
)

packageOptions := Seq(
  Package.ManifestAttributes(
    ("Plugin-Class", "de.jowisoftware.mining.linker.trac.TracStyleLinker"),
    ("Plugin-Type", "Linker"),
    ("Plugin-Name", "Trac-Style")
  )
)
