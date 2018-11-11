package sae.benchmark.hospital

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.algebra
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.syntax.iql.IR

import sae.benchmark.BenchmarkMultiNodeSpec

class HospitalBenchmark3MultiJvmNode1 extends HospitalBenchmark3
class HospitalBenchmark3MultiJvmNode2 extends HospitalBenchmark3
class HospitalBenchmark3MultiJvmNode3 extends HospitalBenchmark3
class HospitalBenchmark3MultiJvmNode4 extends HospitalBenchmark3

object HospitalBenchmark3 {} // this object is necessary for multi-node testing

//Everything (except the tables) is on the client
class HospitalBenchmark3 extends MultiNodeSpec(HospitalMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with HospitalBenchmark {

	override val benchmarkQuery = "query3"

	import HospitalMultiNodeConfig._

	//Setup query environment
	val personHost = RemoteHost("personHost", node(node1))
	val patientHost = RemoteHost("patientHost", node(node2))
	val knowledgeHost = RemoteHost("knowledgeHost", node(node3))
	val clientHost = RemoteHost("clientHost", node(node4))

	implicit val env: QueryEnvironment = QueryEnvironment.create(
		system,
		Map(
			personHost -> (0, Set("red")),
			patientHost -> (0, Set("red", "green", "purple")),
			knowledgeHost -> (0, Set("purple")),
			clientHost -> (1, Set("white", "red", "green", "purple"))
		)
	)

	override type ResultType = (Int, String, String)

	object ClientNode extends ReceiveNode[ResultType]("client") {
		override def relation(): idb.Relation[ResultType] = {
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

			val query3 =
				SELECT DISTINCT (
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) =>
						(person.personId, person.name, knowledgeData.diagnosis)
					) FROM(RECLASS(personDB, Taint("white")), UNNEST(RECLASS(patientDB, Taint("white")), (x: Rep[PatientType]) => x.symptoms), RECLASS(knowledgeDB, Taint("white"))
				) WHERE (
					(person: Rep[PersonType], patientSymptom: Rep[(PatientType, String)], knowledgeData: Rep[KnowledgeType]) =>
						person.personId == patientSymptom._1.personId AND
							patientSymptom._2 == knowledgeData.symptom AND
							knowledgeData.symptom == Symptoms.cough AND
							person.name == "John Doe"
					)


			//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
			val r: idb.Relation[ResultType] =
				ROOT(clientHost, query3)
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


