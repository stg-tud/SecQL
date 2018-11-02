package sae.benchmark.company

import idb.query.taint.Taint
import sae.benchmark.BenchmarkConfig

sealed trait CompanyConfig extends BenchmarkConfig {

	override val debugMode: Boolean = false

	override val benchmarkGroup = "company"

	override val doWarmup: Boolean = true
	override val iterations: Int = 50

	override val mongoTransferRecords: Boolean = true
	override val mongoConnectionString: String = "mongodb://server.company.i3ql:27017"

	val priorityPublic : Int
	val priorityProduction : Int
	val priorityPurchasing : Int
	val priorityEmployees : Int
	val priorityClient : Int

	val permissionsPublic : Set[String]
	val permissionsProduction : Set[String]
	val permissionsPurchasing : Set[String]
	val permissionsEmployees : Set[String]
	val permissionsClient : Set[String]

	val labelPublic : Taint
	val labelProduction : Taint
	val labelPurchasing : Taint
	val labelEmployees : Taint
}

trait DefaultPriorityConfig extends CompanyConfig {
	override val priorityPublic : Int = 4
	override val priorityProduction : Int = 4
	override val priorityPurchasing : Int = 4
	override val priorityEmployees : Int = 4
	override val priorityClient : Int = 12

	override val permissionsPublic : Set[String] = Set("lab:public")
	override val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient : Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	override val labelPublic : Taint = Taint("lab:public")
	override val labelProduction : Taint = Taint("lab:production")
	override val labelPurchasing : Taint = Taint("lab:purchasing")
	override val labelEmployees : Taint = Taint("lab:employees")
}

trait PublicPriorityConfig extends CompanyConfig {
	override val priorityPublic : Int = 4
	override val priorityProduction : Int = 4
	override val priorityPurchasing : Int = 4
	override val priorityEmployees : Int = 4
	override val priorityClient : Int = 12

	override val permissionsPublic : Set[String] = Set("lab:public")
	override val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient : Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	override val labelPublic : Taint = Taint("lab:public")
	override val labelProduction : Taint = Taint("lab:public")
	override val labelPurchasing : Taint = Taint("lab:public")
	override val labelEmployees : Taint = Taint("lab:public")
}

trait ClientPriorityConfig extends CompanyConfig {
	override val priorityPublic : Int = 1
	override val priorityProduction : Int = 1
	override val priorityPurchasing : Int = 1
	override val priorityEmployees : Int = 1
	override val priorityClient : Int = 0

	override val permissionsPublic : Set[String] = Set("lab:public")
	override val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient : Set[String] = Set("lab:client")

	override val labelPublic : Taint = Taint("lab:client")
	override val labelProduction : Taint = Taint("lab:client")
	override val labelPurchasing : Taint = Taint("lab:client")
	override val labelEmployees : Taint = Taint("lab:client")
}