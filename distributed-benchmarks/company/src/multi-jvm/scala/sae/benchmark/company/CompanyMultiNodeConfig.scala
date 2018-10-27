package sae.benchmark.company

import akka.remote.testconductor.RoleName
import sae.benchmark.BenchmarkMultiNodeConfig

object CompanyMultiNodeConfig extends BenchmarkMultiNodeConfig {
	val rolePublic: RoleName = role("role:public")
	val roleProduction: RoleName = role("role:production")
	val rolePurchasing: RoleName = role("role:purchasing")
	val roleEmployees: RoleName = role("role:employees")
	val roleClient: RoleName = role("role:client")
}
