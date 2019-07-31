name := "distributed-benchmarks"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % lmsVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.4.2",
    "net.liftweb" %% "lift-json" % "3.3.0"
)

parallelExecution in Test := false

logBuffered in Test := false