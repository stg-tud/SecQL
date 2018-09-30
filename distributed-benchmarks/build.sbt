name := "distributed-benchmarks"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % lmsVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
)

parallelExecution in Test := false

logBuffered in Test := false