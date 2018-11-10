package sae.benchmark

import akka.remote.testkit.MultiNodeSpec
import idb.metrics.{CountEvaluator, ThroughputEvaluator}
import idb.query.QueryEnvironment
import idb.{BagTable, Relation, Table}
import sae.benchmark.recording.mongo.MongoTransport
import sae.benchmark.recording.recorders._

/**
  * Created by mirko on 07.11.16.
  */
trait Benchmark extends MultiNodeSpec with BenchmarkConfig {

	//Environment must be available everywhere
	implicit val env: QueryEnvironment

	def initialParticipants: Int = roles.size

	/*
		Node definitions
	 */
	class Node(val nodeName: String) {
		val eventRecorder = new EventRecorder(executionId, nodeName,
			if (mongoTransferRecords) new MongoTransport[EventRecord](mongoConnectionString, EventRecord) else null)
		val performanceRecorder = new PerformanceRecorder(executionId, nodeName,
			if (mongoTransferRecords) new MongoTransport[PerformanceRecord](mongoConnectionString, PerformanceRecord) else null)
		var throughputRecorder: ThroughputRecorder = _

		private var currentSection: String = null
		private def enterSection(section: String): Unit = {
			if (currentSection != null) {
				eventRecorder.log(s"section.$currentSection.exit")
				log.info(s"Exiting section '$currentSection' on node '$nodeName'")
			}

			enterBarrier(section: String)

			eventRecorder.log(s"section.$section.enter")
			log.info(s"Entering section '$section' on node '$nodeName'")
			currentSection = section
		}

		def exec(): Unit = {
			deploy()
			compile()
			if (doWarmup) {
				warmupInit()
				warmup()
				warmupAfterBurn()
				warmupFinished()
				reset()
			}
			measurementInit()
			measurementRecordingInit()
			measurement()
			measurementAfterBurn()
			measurementFinished()
		}

		protected def deploy(): Unit = {
			enterSection("deploy")
		}

		protected def compile(): Unit = {
			enterSection("compile")
		}

		protected def warmupInit(): Unit = {
			enterSection("warmup-init")
		}

		protected def warmup(): Unit = {
			enterSection("warmup")
		}

		protected def warmupAfterBurn(): Unit = {
			enterSection("warmup-after-burn")
		}

		protected def warmupFinished(): Unit = {
			enterSection("warmup-finish")
		}

		protected def reset(): Unit = {
			enterSection("reset")
		}

		protected def measurementInit(): Unit = {
			enterSection("measurement-init")
		}

		/**
		  * Starts the measurement related recording
		  *
		  * @param recorderRelations Relations to record the throughput of
		  */
		protected def measurementRecordingInit(recorderRelations: Map[String, ThroughputEvaluator[_]] = null): Unit = {
			enterSection("measurement-recording")

			if (recorderRelations != null) {
				throughputRecorder = new ThroughputRecorder(executionId, nodeName, recorderRelations,
					if (mongoTransferRecords) new MongoTransport[ThroughputRecord](mongoConnectionString, ThroughputRecord) else null)
				throughputRecorder.start(throughputRecordingIntervalMs)
			}

			performanceRecorder.start(performanceRecordingIntervalMs)
		}

		protected def measurement(): Unit = {
			enterSection("measurement")
		}

		protected def measurementAfterBurn(): Unit = {
			enterSection("measurement-after-burn")
		}

		protected def measurementFinished(): Unit = {
			enterSection("measurement-finished")
			log.info("Recording configuration")
			val configRecorder = new ConfigRecorder(executionId, nodeName, new MongoTransport[ConfigRecord](mongoConnectionString, ConfigRecord))
			configRecorder.log(Benchmark.this)

			log.info("Transferring recordings to central database")
			configRecorder.terminateAndTransfer()
			performanceRecorder.terminateAndTransfer()
			eventRecorder.terminateAndTransfer()
			if (throughputRecorder != null)
				throughputRecorder.terminateAndTransfer()
		}
	}

	class DBNode(
					nodeName: String,
					val dbNames: Seq[String],
					val initIterations: Int,
					val iterations: Int
				) extends Node(nodeName) {

		protected def initIteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {}

		protected def iteration(dbs: Seq[Table[Any]], iteration: Int): Unit = {}

		var dbs: Seq[Table[Any]] = _

		override protected def deploy(): Unit = {
			super.deploy()

			import idb.syntax.iql._
			dbs = Seq.fill(dbNames.size)(BagTable.empty[Any])
			dbs.zip(dbNames) foreach (t => REMOTE DEFINE(t._1, t._2))
		}

		override protected def warmupInit(): Unit = {
			super.warmupInit()
			(0 until initIterations).foreach(i => initIteration(dbs, i))
		}

		override protected def warmup(): Unit = {
			super.warmup()
			(0 until iterations).foreach(i => iteration(dbs, i))
		}

		override protected def measurementRecordingInit(recorderRelations: Map[String, ThroughputEvaluator[_]]): Unit =
			super.measurementRecordingInit(Map(dbNames zip dbs map { db => db._1 -> new ThroughputEvaluator(db._2) }: _*))

		override protected def measurementInit(): Unit = {
			super.measurementInit()
			(0 until initIterations).foreach(i => initIteration(dbs, i))
		}

		override protected def measurement(): Unit = {
			super.measurement()
			(0 until iterations).foreach(i => iteration(dbs, i))
		}
	}

	abstract class ReceiveNode[Domain](nodeName: String) extends Node(nodeName) {

		protected def relation(): Relation[Domain]

		protected var r: Relation[Domain] = _

		protected var countEvaluator: CountEvaluator[Domain] = _

		protected def sleepUntilCold() {
			var lastEventCount = 0L
			do {
				lastEventCount = countEvaluator.eventCount
				log.info(s"Waiting to get cold... ($lastEventCount events)")
				Thread.sleep(waitForBeingColdMs)
			} while (lastEventCount != countEvaluator.eventCount)
			log.info(s"I am cold ($lastEventCount events)")
		}

		override protected def compile(): Unit = {
			super.compile()

			r = relation()
			r.print()
			log.info(s"Waiting for deployment ${waitForDeploymentMs / 1000}s")
			Thread.sleep(waitForDeploymentMs)
		}

		override def warmupInit(): Unit = {
			super.warmupInit()
			countEvaluator = new CountEvaluator[Domain](r)
		}

		override protected def warmupAfterBurn(): Unit = {
			super.warmupAfterBurn()
			sleepUntilCold()
			r.removeObserver(countEvaluator)
			countEvaluator = null
		}

		override protected def reset(): Unit = {
			super.reset()
			r.reset()
			log.info(s"Waiting for reset ${waitForResetMs / 1000}s")
			Thread.sleep(waitForResetMs)
		}

		override protected def measurementInit(): Unit = {
			super.measurementInit()
			if (debugMode) idb.util.printEvents(r, "result")
		}

		override protected def measurementRecordingInit(recorderRelations: Map[String, ThroughputEvaluator[_]] = null): Unit = {
			countEvaluator = new CountEvaluator[Domain](r)
			super.measurementRecordingInit(Map("relation" -> new ThroughputEvaluator[Domain](r, countEvaluator)))
		}

		override protected def measurement(): Unit = {
			super.measurement()
		}

		override protected def measurementAfterBurn(): Unit = {
			super.measurementAfterBurn()
			sleepUntilCold()
		}
	}


}
