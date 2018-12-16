package sae.benchmark.hospital

import sae.benchmark.BenchmarkConfig

trait HospitalConfig extends BenchmarkConfig {

	val baseIterations: Int = 1000000 // Should be dividable by personSelectionInterval
	// In which interval person records shall be produced, that match the selection criteria of query1 - query4
	val personSelectionInterval: Int = 4

	override val debugMode: Boolean = false
	override val dbBackpressure: Boolean = true

	override val benchmarkGroup = "hospital"

	override val doWarmup: Boolean = true

	override val mongoTransferRecords: Boolean = true
	override val mongoConnectionString: String = "mongodb://server.hospital.i3ql:27017"

}