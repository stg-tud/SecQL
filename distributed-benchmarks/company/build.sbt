name := "company-benchmark"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % lmsVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
)

// Use entire memory configured as container limit
jvmOptions in MultiJvm := Seq("-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=1", "-XshowSettings:vm")

parallelExecution in Test := false

logBuffered in Test := false

multiNodeHostsFileName in MultiJvm := "distributed-benchmarks/company/nodes.hosts"