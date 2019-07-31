package sae.benchmark.db

import idb.BagTable

abstract class BenchmarkDB[T](_config: BenchmarkDBConfig[T]) extends BagTable[T] {
	val name: String = _config.name
	val config: BenchmarkDBConfig[T] = _config
}