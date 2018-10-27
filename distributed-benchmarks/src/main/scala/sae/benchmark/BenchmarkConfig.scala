package sae.benchmark

/**
  * Created by mirko on 07.11.16.
  */
trait BenchmarkConfig {

	val debugMode: Boolean

	val benchmarkGroup: String
	val benchmarkQuery: String

	val iterations: Int
	val doWarmup: Boolean

	val waitForDeploymentMs = 10000L
	val waitForResetMs = 5000L
	val waitForBeingColdMs = 2000L

	val throughputRecordingIntervalMs = 100
	val performanceRecordingIntervalMs = 100
	val mongoTransferRecords: Boolean = false
	val mongoConnectionString: String = "mongodb://localhost:27017"

	val startTime: Long = System.currentTimeMillis() / 1000

	def executionId: String = {
		s"$benchmarkGroup.$benchmarkQuery.$startTime"
	}
}


