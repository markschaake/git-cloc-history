enablePlugins(JavaAppPackaging)

scalaVersion := "2.11.7"

name := "git-cloc-history"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.3.0",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12"
 )
