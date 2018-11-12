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

	val waitForDeploymentMs = 15000L
	val waitForResetMs = 5000L
	val waitForBeingColdIntervalMs = 5000L // Interval in which it is checked, whether the receiving relation is cold
	val waitForBeingColdTimeoutMs = 30000L // Timeout after which test fails, if relation does not get cold and eventCount doesn't change

	val throughputRecordingIntervalMs = 100
	val performanceRecordingIntervalMs = 100
	val latencyRecordingInterval = 100 // Interval of results that shall be recorded for latency analysis

	val mongoTransferRecords: Boolean = false
	val mongoConnectionString: String

	// Append unique id, if runs shall be distinguishable
	def executionId: String = {
		s"$benchmarkGroup.$benchmarkQuery.xxxx"
	}

}


