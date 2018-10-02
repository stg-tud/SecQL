name := "tpch-benchmark"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % lmsVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
)

jvmOptions in MultiJvm := Seq("-Xmx1024M", "-Xms256M")

parallelExecution in Test := false

logBuffered in Test := false

multiNodeHostsFileName in MultiJvm := "distributed-benchmarks/tpch/nodes.hosts"