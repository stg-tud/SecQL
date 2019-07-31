/** Project */
name := "analyses-metrics"

version := "0.0.1"

organization := "de.tud.cs.st"

parallelExecution in Test := false

logBuffered in Test := false

libraryDependencies in ThisBuild ++= Seq(
	"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
	"de.tud.cs.st" %% "idb-runtime" % "latest.integration",
	"de.tud.cs.st" %% "test-data" % "latest.integration"
)