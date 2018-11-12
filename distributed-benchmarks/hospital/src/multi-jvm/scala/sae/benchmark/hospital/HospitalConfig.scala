package sae.benchmark.hospital

import sae.benchmark.BenchmarkConfig

trait HospitalConfig extends BenchmarkConfig {

	override val debugMode: Boolean = false

	override val benchmarkGroup = "hospital"

	override val doWarmup: Boolean = true
	override val iterations: Int = 10000 // Should be dividable by personSelectionInterval

	override val mongoTransferRecords: Boolean = true
	override val mongoConnectionString: String = "mongodb://server.hospital.i3ql:27017"

	// In which interval person records shall be produced, that match the selection criteria of query1 - query4
	val personSelectionInterval: Int = 4

}