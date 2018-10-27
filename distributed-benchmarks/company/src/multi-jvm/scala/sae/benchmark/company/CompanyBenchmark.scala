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
		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val productDB = dbs(0)
			val factoryDB = dbs(1)

			productDB += Product(index, Data.ProductNames.BILLY + index)
			productDB += Product(iterations + index, Data.ProductNames.KNUT + index)

			if (index % 10 < 5)
				factoryDB += Factory(index, Data.Cities.FRANKFURT)
			else
				factoryDB += Factory(index, Data.Cities.DARMSTADT)

		}
	}

	object ProductionDBNode extends DBNode("production", Seq("component-db", "pc-db", "fp-db"), 0, iterations) {
		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val componentsDB = dbs(0)
			val pcDB = dbs(1)
			val fpDB = dbs(2)

			componentsDB += Component(index, Data.ComponentNames.OAK + index, Data.Materials.WOOD)
			componentsDB += Component(iterations + index, Data.ComponentNames.IRON + index, Data.Materials.WOOD)

			val i = index % 10
			pcDB += PC(index, index, i + 1)
			pcDB += PC(index, iterations + index, i + 1)
			pcDB += PC(iterations + index, iterations + index, i + 1)

			fpDB += FP(index, index)
			fpDB += FP(index, iterations + index)
		}
	}

	object PurchasingDBNode extends DBNode("purchasing", Seq("supplier-db", "sc-db"), 0, iterations) {
		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val supplierDB = dbs(0)
			val scDB = dbs(1)

			val i = index % 10
			i match {
				case 0 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.FRANKFURT)
				case 1 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.FRANKFURT)
				case 2 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.FRANKFURT)
				case 3 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.FRANKFURT)
				case 4 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.DARMSTADT)
				case 5 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.DARMSTADT)
				case 6 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.HANAU)
				case 7 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.HANAU)
				case 8 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.OFFENBACH)
				case 9 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.OFFENBACH)
			}

			scDB += SC(index, index, i + 1, (i + 1) * 10)
			scDB += SC(index, iterations + index, i + 1, (i + 1) * 10)
		}
	}

	object EmployeesDBNode extends DBNode("employees", Seq("employee-db", "fe-db"), 0, iterations) {
		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val employeesDB = dbs(0)
			val feDB = dbs(1)

			val n = index * 10
			(0 until 10) foreach (i => {
				employeesDB += Employee(n + i, Data.EmployeeNames.ALICE + (n + i))
				if (i == 0)
					feDB += FE(index, n + i, Data.Jobs.ACCOUNTANT)
				else
					feDB += FE(index, n + i, Data.Jobs.WORKER)
			})
		}
	}

}
