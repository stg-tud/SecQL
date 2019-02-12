package sae.benchmark.company

import idb.query.RemoteHost
import idb.schema.company.{Product, _}
import sae.benchmark.Benchmark
import sae.benchmark.company.CompanyMultiNodeConfig._
import sae.benchmark.db.BenchmarkDBConfig


trait CompanyBenchmark extends Benchmark with NoPrivacyConfig {

	val baseIterationsCoefficient: Float = 1f
	override val benchmarkGroup = "company"
	// Must be lazy val to ensure overridden baseIterationsCoefficient values have effect
	lazy val baseIterations: Int = (baseIterationsCoefficient * groundIterations).toInt

	//Setup query environment
	val publicHost = RemoteHost("public-host", node(rolePublic))
	val productionHost = RemoteHost("production-host", node(roleProduction))
	val purchasingHost = RemoteHost("purchasing-host", node(rolePurchasing))
	val employeesHost = RemoteHost("employees-host", node(roleEmployees))
	val clientHost = RemoteHost("clients-host", node(roleClient))

	object BaseCompany extends CompanySchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends CompanyTestData

	class PublicDBNode extends DBNode("public") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"product-db",
				0, null,
				2 * baseIterations, (db, iteration) => {
					var productId: Int = iteration / 2
					var productName = Data.ProductNames.BILLY + productId

					if (iteration % 2 == 1) {
						productName = Data.ProductNames.KNUT + productId
						productId += baseIterations
					}

					db += Product(productId, productName)
					addProductHook(productId)
				},
				1, 2
			),
			BenchmarkDBConfig(
				"factory-db",
				0, null,
				baseIterations, (db, iteration) => {
					if (iteration % 10 < 5)
						db += Factory(iteration, Data.Cities.FRANKFURT)
					else
						db += Factory(iteration, Data.Cities.DARMSTADT)
					addFactoryHook(iteration)
				}
			)
		)

		protected def addProductHook(productId: Int): Unit = {}

		protected def addFactoryHook(factoryId: Int): Unit = {}
	}

	class ProductionDBNode extends DBNode("production") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"component-db",
				0, null,
				2 * baseIterations, (db, iteration) => {
					var componentId: Int = iteration / 2
					var componentName = Data.ComponentNames.OAK + componentId

					if (iteration % 2 == 1) {
						componentName = Data.ComponentNames.IRON + componentId
						componentId += baseIterations
					}

					db += Component(componentId, componentName, Data.Materials.WOOD)
					addComponentHook(componentId)
				},
				1, 2
			),
			BenchmarkDBConfig(
				"pc-db",
				0, null,
				3 * baseIterations, (db, iteration) => {
					var productId: Int = iteration / 3
					var componentId: Int = iteration / 3

					iteration % 3 match {
						case 0 =>
						case 1 =>
							componentId += baseIterations
						case 2 =>
							productId += baseIterations
							componentId += baseIterations
					}

					db += PC(productId, componentId, (iteration / 3) % 10 + 1)
					addPCHook(productId, componentId)
				},
				1, 3
			),
			BenchmarkDBConfig(
				"fp-db",
				0, null,
				2 * baseIterations, (db, iteration) => {
					val factoryId: Int = iteration / 2
					var productId: Int = iteration / 2

					if (iteration % 2 == 1)
						productId += baseIterations

					db += FP(factoryId, productId)
					addFPHook(factoryId, productId)
				},
				1, 2
			)
		)

		protected def addComponentHook(componentId: Int): Unit = {}

		protected def addPCHook(productId: Int, componentId: Int): Unit = {}

		protected def addFPHook(factoryId: Int, productId: Int): Unit = {}
	}

	class PurchasingDBNode extends DBNode("purchasing") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"supplier-db",
				0, null,
				baseIterations, (db, iteration) => {
					iteration % 10 match {
						case 0 => db += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.FRANKFURT)
						case 1 => db += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.FRANKFURT)
						case 2 => db += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.FRANKFURT)
						case 3 => db += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.FRANKFURT)
						case 4 => db += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.DARMSTADT)
						case 5 => db += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.DARMSTADT)
						case 6 => db += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.HANAU)
						case 7 => db += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.HANAU)
						case 8 => db += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.OFFENBACH)
						case 9 => db += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.OFFENBACH)
					}
					addSupplierHook(iteration)
				}
			),
			BenchmarkDBConfig(
				"sc-db",
				0, null,
				2 * baseIterations, (db, iteration) => {
					val supplierId: Int = iteration / 2
					var componentId: Int = iteration / 2
					val inventory: Int = iteration / 2 % 10 + 1
					val price: Int = (iteration / 2 % 10 + 1) * 10

					if (iteration % 2 == 1)
						componentId += baseIterations

					db += SC(supplierId, componentId, inventory, price)
					addSCHook(supplierId, componentId)
				},
				1, 2
			)
		)

		protected def addSupplierHook(supplierId: Int): Unit = {}

		protected def addSCHook(supplierId: Int, componentId: Int): Unit = {}
	}

	class EmployeesDBNode extends DBNode("employees") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"employee-db",
				0, null,
				10 * baseIterations, (db, iteration) => {
					val employeeId = iteration
					db += Employee(employeeId, Data.EmployeeNames.ALICE + employeeId)
					addEmployeeHook(employeeId)
				},
				1, 10
			),
			BenchmarkDBConfig(
				"fe-db",
				0, null,
				10 * baseIterations, (db, iteration) => {
					val factoryId: Int = iteration / 10
					val employeeId = iteration
					var job = Data.Jobs.WORKER

					if (iteration % 10 == 0)
						job = Data.Jobs.ACCOUNTANT

					db += FE(factoryId, employeeId, job)
					addFEHook(factoryId, employeeId)
				},
				1, 10
			)
		)

		protected def addEmployeeHook(employeeId: Int): Unit = {}

		protected def addFEHook(factoryId: Int, employeeId: Int): Unit = {}
	}

}
