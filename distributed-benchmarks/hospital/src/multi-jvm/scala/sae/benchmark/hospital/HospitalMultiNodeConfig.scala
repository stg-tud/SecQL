package sae.benchmark.hospital

import akka.remote.testconductor.RoleName
import sae.benchmark.BenchmarkMultiNodeConfig

object HospitalMultiNodeConfig extends BenchmarkMultiNodeConfig {
	val node1: RoleName = role("person")
	val node2: RoleName = role("patient")
	val node3: RoleName = role("knowledge")
	val node4: RoleName = role("client")
}