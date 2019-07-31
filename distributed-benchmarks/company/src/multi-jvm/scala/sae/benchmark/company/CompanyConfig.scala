package sae.benchmark.company

import idb.query.taint.Taint
import sae.benchmark.BenchmarkConfig

sealed trait CompanyConfig extends BenchmarkConfig with CompanyPermissionConfig {

	val groundIterations: Int = 1000000

	override val debugMode: Boolean = false
	override val dbBackpressure: Boolean = true

	override val benchmarkGroup = "company"

	override val doWarmup: Boolean = true

	override val mongoTransferRecords: Boolean = true
	override val mongoConnectionString: String = "mongodb://server.company.i3ql:27017"
}

trait CompanyPermissionConfig {
	val priorityPublic: Int
	val priorityProduction: Int
	val priorityPurchasing: Int
	val priorityEmployees: Int
	val priorityClient: Int

	val permissionsPublic: Set[String]
	val permissionsProduction: Set[String]
	val permissionsPurchasing: Set[String]
	val permissionsEmployees: Set[String]
	val permissionsClient: Set[String]

	val labelPublic: Taint
	val labelProduction: Taint
	val labelPurchasing: Taint
	val labelEmployees: Taint
}

trait DefaultPriorityConfig extends CompanyConfig {
	// Host priorities
	override val priorityPublic: Int = 4
	override val priorityProduction: Int = 4
	override val priorityPurchasing: Int = 4
	override val priorityEmployees: Int = 4
	override val priorityClient: Int = 12

	// Host permissions
	override val permissionsPublic: Set[String] = Set("lab:public")
	override val permissionsProduction: Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing: Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees: Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient: Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	// DB labels on hosts
	override val labelPublic: Taint = Taint("lab:public")
	override val labelProduction: Taint = Taint("lab:production")
	override val labelPurchasing: Taint = Taint("lab:purchasing")
	override val labelEmployees: Taint = Taint("lab:employees")
}

trait PrivacyAwareConfig extends CompanyConfig {
	// Host priorities
	override val priorityPublic: Int = 1
	override val priorityProduction: Int = 1
	override val priorityPurchasing: Int = 1
	override val priorityEmployees: Int = 1
	override val priorityClient: Int = 1

	// Host permissions
	override val permissionsPublic: Set[String] = Set("lab:public")
	override val permissionsProduction: Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing: Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees: Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient: Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	// DB labels on hosts
	override val labelPublic: Taint = Taint("lab:public")
	override val labelProduction: Taint = Taint("lab:production")
	override val labelPurchasing: Taint = Taint("lab:purchasing")
	override val labelEmployees: Taint = Taint("lab:employees")
}

trait NoPrivacyConfig extends CompanyConfig {
	// Host priorities
	override val priorityPublic: Int = 1
	override val priorityProduction: Int = 1
	override val priorityPurchasing: Int = 1
	override val priorityEmployees: Int = 1
	override val priorityClient: Int = 1

	// Host permissions
	override val permissionsPublic: Set[String] = Set("lab:public")
	override val permissionsProduction: Set[String] = Set("lab:public")
	override val permissionsPurchasing: Set[String] = Set("lab:public")
	override val permissionsEmployees: Set[String] = Set("lab:public")
	override val permissionsClient: Set[String] = Set("lab:public")

	// DB labels on hosts
	override val labelPublic: Taint = Taint("lab:public")
	override val labelProduction: Taint = Taint("lab:public")
	override val labelPurchasing: Taint = Taint("lab:public")
	override val labelEmployees: Taint = Taint("lab:public")
}

trait ClientOnlyConfig extends CompanyConfig {
	// Host priorities
	override val priorityPublic: Int = 1
	override val priorityProduction: Int = 1
	override val priorityPurchasing: Int = 1
	override val priorityEmployees: Int = 1
	override val priorityClient: Int = 1

	// Host permissions
	override val permissionsPublic: Set[String] = Set("lab:public")
	override val permissionsProduction: Set[String] = Set("lab:production")
	override val permissionsPurchasing: Set[String] = Set("lab:purchasing")
	override val permissionsEmployees: Set[String] = Set("lab:employees")
	override val permissionsClient: Set[String] = Set("lab:client")

	// DB labels on hosts
	override val labelPublic: Taint = Taint("lab:client")
	override val labelProduction: Taint = Taint("lab:client")
	override val labelPurchasing: Taint = Taint("lab:client")
	override val labelEmployees: Taint = Taint("lab:client")
}