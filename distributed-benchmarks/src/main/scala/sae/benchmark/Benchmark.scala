package sae.benchmark

import idb.{BagTable, Relation, SetTable, Table}
import idb.query.QueryEnvironment
import idb.util.PrintEvents

/**
  * Created by mirko on 07.11.16.
  */
trait Benchmark extends BenchmarkConfig {

	/*
		Control Variables
	 */
	val waitForCompile = 10000 //ms
	val waitForData = 12000 //ms
	val waitForReset = 10000 //ms
	val waitForGc = 10000 //ms

	val cpuMeasurementInterval = 10 //ms

	val DEBUG = false

	//Environment must be available everywhere
	implicit val env : QueryEnvironment

	/*
		Barrier management
	 */
	protected def internalBarrier(name : String)

	private def section(name : String): Unit = {
		internalBarrier(name : String)
		println(s"### Enter barrier __${name}__ ###")
	}

	/*
		Node definitions
	 */
	trait DBNode {

		val nodeName : String
		val dbNames : Seq[String]

		val iterations : Int

		val isPredata : Boolean

		def iteration(dbs : Seq[Table[Any]], index : Int)

		var finished = false

		def exec(): Unit = {

			section("deploy")
			import idb.syntax.iql._
			val dbs : Seq[Table[Any]] = Seq.fill(dbNames.size)(BagTable.empty[Any])
			dbs.zip(dbNames) foreach (t =>
				REMOTE DEFINE (t._1, t._2)
			)

			section("compile")
			//The query gets compiled here...

			if (warmup) {
				section("warmup-predata")
				if (isPredata) {
					(0 until iterations).foreach(i => iteration(dbs, i))
				}

				section("warmup-data")
				if (!isPredata) {
					(0 until iterations).foreach(i => iteration(dbs, i))
				}

				section("warmup-finish")

				section("reset")
			}

			section("measure-predata")
			if (isPredata) {
				(0 until iterations).foreach(i => iteration(dbs, i))
			}

			section("measure-init")

					section("measure-data")
					if (!isPredata) {
						(0 until iterations).foreach(i => iteration(dbs, i))
					}
					section("measure-finish")

			section("finish")
		}
	}

	object IntermediateNode {
		def exec(): Unit = {
			section("deploy")
			section("compile")
			if (warmup) {
				section("warmup-predata")
				section("warmup-data")
				section("warmup-finish")
				section("reset")
			}

			section("measure-predata")
			section("measure-init")
			section("measure-data")
			section("measure-finish")
			section("finish")
		}

	}

	trait ReceiveNode[Domain] {

		def relation() : Relation[Domain]
		def eventStartTime(e : Domain) : Long

		var finished = false

		def exec(): Unit = {
			section("deploy")

			section("compile")
			val r : Relation[Domain] = relation()
			//Print the runtime class representation
			Thread.sleep(waitForCompile)

			Predef.println("### Relation.compiled ###")
			r.print()
			Thread.sleep(2000)
			Predef.println("### ###")

			if (warmup) {
				section("warmup-predata")
				//The tables are now sending data
				Thread.sleep(waitForData)

				section("warmup-data")
				//The tables are now sending data
				Thread.sleep(waitForData)

				section("warmup-finish")

				section("reset")
				r.reset()
				Thread.sleep(waitForReset)
			}

			section("measure-predata")
			//The tables are now sending data
			Thread.sleep(waitForData)

			section("measure-init")

			if (DEBUG)
				idb.util.printEvents(r, "result")


					section("measure-data")
					Thread.sleep(waitForData)

					section("measure-finish")


			section("finish")

		}
	}




}
