package sae.benchmark

import idb.metrics.ProcessPerformance

/**
  * Created by mirko on 07.11.16.
  */
trait BenchmarkConfig {

	val debugMode: Boolean
	val dbBackpressure: Boolean

	val benchmarkGroup: String
	val benchmarkQuery: String

	val doWarmup: Boolean

	val waitForBeingColdIntervalMs = 5000L // Interval in which it is checked, whether the receiving relation is cold
	val waitForBeingColdTimeoutMs = 30000L // Timeout after which test fails, if relation does not get cold and eventCount doesn't change

	val throughputRecordingIntervalMs = 100
	val performanceRecordingIntervalMs = 100
	val latencyRecordingInterval = 1000 // Interval of results that shall be recorded for latency analysis

	val mongoTransferRecords: Boolean = false
	val mongoConnectionString: String

	// Mainly included in config in order to get recorded as config property
	val memory: Long = ProcessPerformance.availableMemory
	val cores: Int = ProcessPerformance.availableCores

	// Append unique id, if runs shall be distinguishable
	def executionId: String = {
		s"$benchmarkGroup.$benchmarkQuery.xxxx"
	}

}