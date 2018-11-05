package sae.benchmark.hospital

import idb.Table
import idb.schema.hospital
import idb.schema.hospital._
import sae.benchmark.Benchmark

/**
  * Barriers that are used in the hospital benchmark:
  *
  * deployed - The tables have been deployed on their servers and the printer has been initialized.
  * compiled - The query has been compiled and deployed to the servers.
  *
  * sent-warmup - The warmup events have been sent (from the tables)
  *
  * resetted - The warmup events have been received and the data structures have been resetted.
  *
  * ready-measure - The classes needed for measurements have been initialized.
  * sent-measure - The measure events have been sent (from the tables).
  *
  * finished - The measurement has been finished.
  *
  *
  * deploy
  * compile
  * warmup-predata
  * warmup-data
  * warmup-finish
  * reset
  * measure-predata
  * measure-init
  * measure-data
  * measure-finish
  * finish
  */
trait HospitalBenchmark extends Benchmark with HospitalConfig {

	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends HospitalTestData


	type PersonType = (Long, Person)
	type PatientType = Patient
	type KnowledgeType = KnowledgeData
	type ResultType

	val numberOfJohnDoes = 2500

	object PersonDBNode extends DBNode("person", Seq("person-db"), 0, iterations) {

		private val interval = 4//iterations / numberOfJohnDoes

		private var count = 0

		override protected def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val db = dbs.head
			if (count == interval)
				count = 0

			if (count == 0)
				db += ((System.currentTimeMillis(), hospital.Person(iteration, "John Doe", 1973)))
			else
				db += ((System.currentTimeMillis(), hospital.Person(iteration, "Jane Doe", 1960)))

			count += 1
		}
	}

	object PatientDBNode extends DBNode("patient", Seq("patient-db"), 0, iterations) {

		import Data.Symptoms

		override protected def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val db = dbs.head
			db += hospital.Patient(iteration, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))
		}
	}

	object KnowledgeDBNode extends DBNode("knowledge", Seq("knowledge-db"), 1, 0) {

		import Data.lungCancer1

		override protected def initIteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {
			val db = dbs.head
			db += lungCancer1
		}
	}

}
