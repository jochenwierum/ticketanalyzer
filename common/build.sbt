name := "ticketanalyzer-common"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.clapper" %% "grizzled-slf4j" % "0.6.8",
  "org.slf4j" % "slf4j-log4j12" % "1.6.0"
)

retrieveManaged := true
