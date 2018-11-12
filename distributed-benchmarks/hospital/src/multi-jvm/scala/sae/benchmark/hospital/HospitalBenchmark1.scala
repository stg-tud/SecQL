package sae.benchmark.hospital

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.query.QueryEnvironment
import idb.query.taint._
import sae.benchmark.BenchmarkMultiNodeSpec
import sae.benchmark.hospital.HospitalMultiNodeConfig._

class HospitalBenchmark1MultiJvmNode1 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode2 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode3 extends HospitalBenchmark1
class HospitalBenchmark1MultiJvmNode4 extends HospitalBenchmark1

object HospitalBenchmark1 {} // this object is necessary for multi-node testing

//Selection is pushed down == events get filtered before getting sent
class HospitalBenchmark1 extends MultiNodeSpec(HospitalMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with HospitalBenchmark {

	override val benchmarkQuery = "query1"

	implicit val env: QueryEnvironment = QueryEnvironment.create(
		system,
		Map(
			personHost -> (1, Set("red")),
			patientHost -> (1, Set("red", "green", "purple")),
			knowledgeHost -> (1, Set("purple")),
			clientHost -> (4, Set("red", "green", "purple"))
		)
	)

	object ClientNode extends ReceiveNode[ResultType]("client") with HospitalReceiveNode {
		override def relation(): Relation[ResultType] = {
			//Write an i3ql query...
			import BaseHospital._
			import Data._
			import idb.syntax.iql.IR._
			import idb.syntax.iql._

			val personDB: Rep[Query[PersonType]] =
				REMOTE GET(personHost, "person-db", Taint("red"))
			val patientDB: Rep[Query[PatientType]] =
				REMOTE GET(patientHost, "patient-db", Taint("green"))
			val knowledgeDB: Rep[Query[KnowledgeType]] =
				REMOTE GET(knowledgeHost, "knowledge-db", Taint("purple"))

			val query1 =
				SELECT DISTINCT (
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) =>
						(person.personId, person.name, knowledgeData.diagnosis)
					) FROM(personDB, UNNEST(patientDB, (x: Rep[PatientType]) => x.symptoms), knowledgeDB
				) WHERE (
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom AND
							knowledgeData.symptom == Symptoms.cough AND
							person.name == "John Doe"
					)

			//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
			val r: idb.Relation[ResultType] =
				ROOT(clientHost, query1)
			r
		}
	}

	"Hospital Benchmark" must {
		"run benchmark" in {
			runOn(node1) { PersonDBNode.exec() }
			runOn(node2) { PatientDBNode.exec()	}
			runOn(node3) { KnowledgeDBNode.exec() }
			runOn(node4) { ClientNode.exec() }
		}
	}
}
