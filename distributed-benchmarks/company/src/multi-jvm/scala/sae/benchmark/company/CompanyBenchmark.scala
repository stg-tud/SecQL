package sae.benchmark.company

import idb.Table
import idb.query.RemoteHost
import idb.schema.company._
import sae.benchmark.Benchmark
import sae.benchmark.company.CompanyMultiNodeConfig._

/**
  * Created by mirko on 07.11.16.
  */
trait CompanyBenchmark extends Benchmark with CompanyPermissionConfig {

	override val benchmarkGroup = "company"

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

	class PublicDBNode extends DBNode("public", Seq("product-db", "factory-db"), 0, iterations) {

		protected def addProductHook(productId: Int): Unit = {}

		protected def addFactoryHook(factoryId: Int): Unit = {}

		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val productDB = dbs(0)
			val factoryDB = dbs(1)

			// Publish two products in each iteration
			productDB += Product(iteration, Data.ProductNames.BILLY + iteration)
			addProductHook(iteration)
			productDB += Product(iterations + iteration, Data.ProductNames.KNUT + iteration)
			addProductHook(iterations + iteration)

			// Publish factory Frankfurt at iterations 0, 10, ..., 40 and Darmstadt at every 10th afterwards
			if (iteration % 10 < 5)
				factoryDB += Factory(iteration, Data.Cities.FRANKFURT)
			else
				factoryDB += Factory(iteration, Data.Cities.DARMSTADT)
			addFactoryHook(iteration)

		}
	}

	class ProductionDBNode extends DBNode("production", Seq("component-db", "pc-db", "fp-db"), 0, iterations) {

		protected def addComponentHook(componentId: Int): Unit = {}

		protected def addPCHook(productId: Int, componentId: Int): Unit = {}

		protected def addFPHook(factoryId: Int, productId: Int): Unit = {}

		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val componentsDB = dbs(0)
			val pcDB = dbs(1)
			val fpDB = dbs(2)

			// Publish two components at every iteration
			componentsDB += Component(iteration, Data.ComponentNames.OAK + iteration, Data.Materials.WOOD)
			addComponentHook(iteration)
			componentsDB += Component(iterations + iteration, Data.ComponentNames.IRON + iteration, Data.Materials.WOOD)
			addComponentHook(iterations + iteration)

			// Publish three product components list entries per iteration
			val i = iteration % 10
			pcDB += PC(iteration, iteration, i + 1)
			addPCHook(iteration, iteration)
			pcDB += PC(iteration, iterations + iteration, i + 1)
			addPCHook(iteration, iterations + iteration)
			pcDB += PC(iterations + iteration, iterations + iteration, i + 1)
			addPCHook(iterations + iteration, iterations + iteration)

			// Publish two factory produces product entries per iteration
			fpDB += FP(iteration, iteration)
			addFPHook(iteration, iteration)
			fpDB += FP(iteration, iterations + iteration)
			addFPHook(iteration, iterations + iteration)
		}
	}

	class PurchasingDBNode extends DBNode("purchasing", Seq("supplier-db", "sc-db"), 0, iterations) {

		protected def addSupplierHook(supplierId: Int): Unit = {}

		protected def addSCHook(supplierId: Int, componentId: Int): Unit = {}

		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val supplierDB = dbs(0)
			val scDB = dbs(1)

			// Add one supplier per iteration
			val i = iteration % 10
			i match {
				case 0 => supplierDB += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.FRANKFURT)
				case 1 => supplierDB += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.FRANKFURT)
				case 2 => supplierDB += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.FRANKFURT)
				case 3 => supplierDB += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.FRANKFURT)
				case 4 => supplierDB += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.DARMSTADT)
				case 5 => supplierDB += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.DARMSTADT)
				case 6 => supplierDB += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.HANAU)
				case 7 => supplierDB += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.HANAU)
				case 8 => supplierDB += Supplier(iteration, Data.SupplierNames.BAUHAUS + iteration, Data.Cities.OFFENBACH)
				case 9 => supplierDB += Supplier(iteration, Data.SupplierNames.HORNBACH + iteration, Data.Cities.OFFENBACH)
			}
			addSupplierHook(iteration)

			// Add two supplier delivers component relations per iteration
			scDB += SC(iteration, iteration, i + 1, (i + 1) * 10)
			addSCHook(iteration, iteration)
			scDB += SC(iteration, iterations + iteration, i + 1, (i + 1) * 10)
			addSCHook(iteration, iterations + iteration)
		}
	}

	class EmployeesDBNode extends DBNode("employees", Seq("employee-db", "fe-db"), 0, iterations) {

		protected def addEmployeeHook(employeeId: Int): Unit = {}

		protected def addFEHook(factoryId: Int, employeeId: Int): Unit = {}

		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val employeesDB = dbs(0)
			val feDB = dbs(1)

			// Add 10 employees per iteration
			val n = iteration * 10
			(0 until 10) foreach (i => {
				employeesDB += Employee(n + i, Data.EmployeeNames.ALICE + (n + i))
				addEmployeeHook(n + i)
				if (i == 0)
					feDB += FE(iteration, n + i, Data.Jobs.ACCOUNTANT)
				else
					feDB += FE(iteration, n + i, Data.Jobs.WORKER)
				addFEHook(iteration, n + i)
			})
		}
	}

}
