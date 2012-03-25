name := "neo4j"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at
  "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test"
)
