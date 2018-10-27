package sae.benchmark.hospital

import sae.benchmark.BenchmarkConfig

trait HospitalConfig extends BenchmarkConfig {

	override val debugMode: Boolean = false

	override val benchmarkGroup = "hospital"

	override val doWarmup: Boolean = true
	override val iterations: Int = 50000

}