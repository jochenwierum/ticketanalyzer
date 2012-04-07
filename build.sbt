name := "neo4j"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

 resolvers += "Tmatesoft Maven Repository" at
  "http://maven.tmatesoft.com/content/repositories/releases/"
 
libraryDependencies ++= Seq(
  "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test",
  "org.neo4j" % "neo4j" % "1.7.M01" withSources,
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2",
  "org.tmatesoft.svnkit" % "svnkit" % "1.3.7",
  "org.clapper" %% "grizzled-slf4j" % "0.6.8",
  "org.slf4j" % "slf4j-log4j12" % "1.5.8"
)
