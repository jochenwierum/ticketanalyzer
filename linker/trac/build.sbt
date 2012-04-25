name := "ticketanalyzer-linker-trac"

version := "1.0"

scalaVersion := "2.9.1"

retrieveManaged := true


packageOptions := Seq(
  Package.ManifestAttributes(
    ("Plugin-Class", "de.jowisoftware.mining.linker.trac.TracStyleLinker"),
    ("Plugin-Type", "Linker"),
    ("Plugin-Name", "Trac-Style")
  )
)
