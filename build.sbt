
packageArchetype.java_application

scalaVersion := "2.10.2"

name := "git-cloc-history"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.1.0",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3"
 )
