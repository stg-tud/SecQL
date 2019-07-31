package sae.benchmark.hospital

import idb.metrics.ThroughputEvaluator
import idb.observer.NoOpObserver
import idb.query.RemoteHost
import idb.schema.hospital
import idb.schema.hospital._
import sae.benchmark.Benchmark
import sae.benchmark.db.BenchmarkDBConfig
import sae.benchmark.hospital.HospitalMultiNodeConfig._

trait HospitalBenchmark extends Benchmark with HospitalConfig {

	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends HospitalTestData

	type PersonType = Person
	type PatientType = Patient
	type KnowledgeType = KnowledgeData
	type ResultType = (Int, String, String)

	//Setup query environment
	val personHost = RemoteHost("personHost", node(node1))
	val patientHost = RemoteHost("patientHost", node(node2))
	val knowledgeHost = RemoteHost("knowledgeHost", node(node3))
	val clientHost = RemoteHost("clientHost", node(node4))

	object PersonDBNode extends DBNode("person") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"person-db",
				0, null,
				baseIterations, (db, iteration) => {
					if (iteration % personSelectionInterval == 0) {
						// These records are relevant for the selection results of query1 - query4
						logLatency(iteration / personSelectionInterval, "query")
						db += hospital.Person(iteration, "John Doe", 1973)
					}
					else
						db += hospital.Person(iteration, "Jane Doe", 1960)
				}
			)
		)
	}

	object PatientDBNode extends DBNode("patient") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"patient-db",
				0, null,
				baseIterations, (db, iteration) => {
					// These records are relevant for the selection results of query1 - query4
					if (iteration % personSelectionInterval == 0)
						logLatency(iteration / personSelectionInterval, "query")
					db += hospital.Patient(iteration, 4, 2011, Seq(Data.Symptoms.cough, Data.Symptoms.chestPain))
				}
			)
		)
	}

	object KnowledgeDBNode extends DBNode("knowledge") {
		override protected val dbConfigs: Seq[BenchmarkDBConfig[Any]] = Seq(
			BenchmarkDBConfig(
				"knowledge-db",
				1, (db, _) => db += Data.lungCancer1,
				0, null
			)
		)
	}

	trait HospitalReceiveNode extends ReceiveNode[ResultType] {

		override def measurementRecordingInit(recorderRelations: Map[String, ThroughputEvaluator[_]] = null): Unit = {
			super.measurementRecordingInit(recorderRelations)
			setupLatencyRecording()
		}

		private def setupLatencyRecording(): Unit = {
			r.addObserver(new NoOpObserver[ResultType] {
				override def added(v: (Int, String, String)): Unit = {
					logLatency(v._1 / personSelectionInterval, "query", false)
				}

				override def addedAll(vs: Seq[(Int, String, String)]): Unit = vs foreach added
			})
		}

		override protected def sleepUntilCold(expectedCount: Int, entryMode: Boolean): Unit =
			super.sleepUntilCold(baseIterations / personSelectionInterval)

	}

}
