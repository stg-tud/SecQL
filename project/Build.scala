import sbt._

object sae extends Build {

	/*
	Project IDB
  	*/
	lazy val idb = Project(id = "idb", base = file("idb"))
		.aggregate(runtime, intermediateRepresentation, schemaExamples, runtimeCompiler, syntax, integrationTest)

	lazy val runtime = Project(
		id = "idb-runtime",
		base = file("idb/runtime"))

	lazy val intermediateRepresentation = Project(
		id = "idb-intermediate-representation",
		base = file("idb/intermediate-representation"))

	lazy val schemaExamples = Project(
		id = "idb-schema-examples",
		base = file("idb/schema-examples"))

	lazy val runtimeCompiler = Project(
		id = "idb-runtime-compiler",
		base = file("idb/runtime-compiler"))
		.dependsOn(
			schemaExamples % "compile;test",
			runtime % "compile;test",
			intermediateRepresentation % "compile;test"
		)

	lazy val runtimeDistribution = Project(
		id = "idb-runtime-distribution",
		base = file("idb/runtime-distribution"))
		.dependsOn(
			runtimeCompiler % "compile;test"
		)

	lazy val syntax = Project(
		id = "idb-syntax-iql",
		base = file("idb/syntax-iql"))
		.dependsOn(
			runtimeCompiler % "compile;test",
			runtimeDistribution % "compile;test",
			schemaExamples % "compile;test",
			runtime % "compile;test"
		)

	lazy val integrationTest = Project(
		id = "idb-integration-test",
		base = file("idb/integration-test"))
		.dependsOn(
			schemaExamples % "test",
			syntax % "test",
			intermediateRepresentation % "test"
		)


	/*
	Project Distributed Benchmarks
	*/
	lazy val distributedBenchmarks = Project(id = "distributed-benchmarks", base = file("distributed-benchmarks"))
		.dependsOn(
			syntax,
			runtime,
			schemaExamples,
			intermediateRepresentation
		)

	import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
	import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings

	lazy val companyBenchmark = Project(id = "company-benchmark", base = file("distributed-benchmarks/company"))
		.dependsOn(distributedBenchmarks)
		.settings(multiJvmSettings)
		.configs(MultiJvm)

	lazy val hospitalBenchmark = Project(id = "hospital-benchmark", base = file("distributed-benchmarks/hospital"))
		.dependsOn(distributedBenchmarks)
		.settings(multiJvmSettings)
		.configs(MultiJvm)

	lazy val tpchBenchmark = Project(id = "tpch-benchmark", base = file("distributed-benchmarks/tpch"))
		.dependsOn(distributedBenchmarks)
		.settings(multiJvmSettings)
		.configs(MultiJvm)

	/*
	Project Test Data
	*/

	lazy val testData = Project(id = "test-data", base = file("test-data"))

	/*
	Root Project
	*/
	lazy val root = Project(id = "sae", base = file("."))
		.aggregate(runtime, intermediateRepresentation, schemaExamples, runtimeCompiler, runtimeDistribution, syntax, integrationTest, testData, distributedBenchmarks, companyBenchmark, hospitalBenchmark, tpchBenchmark)


	val virtScala = Option(System.getenv("SCALA_VIRTUALIZED_VERSION")).getOrElse("2.11.2")

	val akkaVersion = "2.4.4"

	val logbackVersion = "1.2.3"

	val lmsVersion = "latest.integration"
}
