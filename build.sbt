import AssemblyKeys._

assemblySettings

scalaVersion := "2.10.0"

name := "git-cloc-history"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "2.1.0",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.3"
 )
