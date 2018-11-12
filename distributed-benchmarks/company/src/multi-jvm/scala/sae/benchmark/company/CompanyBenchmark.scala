package sae.benchmark.company

import idb.Table
import idb.schema.company._
import idb.syntax.iql.IR
import sae.benchmark.Benchmark

/**
  * Created by mirko on 07.11.16.
  */
trait CompanyBenchmark extends Benchmark {

	override val benchmarkGroup = "company"

	object BaseCompany extends CompanySchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends CompanyTestData
	
	object PublicDBNode extends DBNode("public", Seq("product-db", "factory-db"), 0, iterations) {
		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val productDB = dbs(0)
			val factoryDB = dbs(1)

			// Publish two products in each iteration
			productDB += Product(iteration, Data.ProductNames.BILLY + iteration)
			productDB += Product(iterations + iteration, Data.ProductNames.KNUT + iteration)

			// Publish factory Frankfurt at iterations 0, 10, ..., 40 and Darmstadt at every 10th afterwards
			if (iteration % 10 < 5)
				factoryDB += Factory(iteration, Data.Cities.FRANKFURT)
			else
				factoryDB += Factory(iteration, Data.Cities.DARMSTADT)

		}
	}

	object ProductionDBNode extends DBNode("production", Seq("component-db", "pc-db", "fp-db"), 0, iterations) {
		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val componentsDB = dbs(0)
			val pcDB = dbs(1)
			val fpDB = dbs(2)

			// Publish two components at every iteration
			componentsDB += Component(iteration, Data.ComponentNames.OAK + iteration, Data.Materials.WOOD)
			componentsDB += Component(iterations + iteration, Data.ComponentNames.IRON + iteration, Data.Materials.WOOD)

			// Publish three product components list entries per iteration
			val i = iteration % 10
			pcDB += PC(iteration, iteration, i + 1)
			pcDB += PC(iteration, iterations + iteration, i + 1)
			pcDB += PC(iterations + iteration, iterations + iteration, i + 1)

			// Publish two factory produces product entries per iteration
			fpDB += FP(iteration, iteration)
			fpDB += FP(iteration, iterations + iteration)
		}
	}

	object PurchasingDBNode extends DBNode("purchasing", Seq("supplier-db", "sc-db"), 0, iterations) {
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

			// Add two supplier delivers component relations per iteration
			scDB += SC(iteration, iteration, i + 1, (i + 1) * 10)
			scDB += SC(iteration, iterations + iteration, i + 1, (i + 1) * 10)
		}
	}

	object EmployeesDBNode extends DBNode("employees", Seq("employee-db", "fe-db"), 0, iterations) {
		override def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val employeesDB = dbs(0)
			val feDB = dbs(1)

			// Add 10 employees per iteration
			val n = iteration * 10
			(0 until 10) foreach (i => {
				employeesDB += Employee(n + i, Data.EmployeeNames.ALICE + (n + i))
				if (i == 0)
					feDB += FE(iteration, n + i, Data.Jobs.ACCOUNTANT)
				else
					feDB += FE(iteration, n + i, Data.Jobs.WORKER)
			})
		}
	}

}
