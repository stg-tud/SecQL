package sae.benchmark.recording.impl

import idb.metrics.ThroughputEvaluator
import sae.benchmark.recording.transport.Transport
import sae.benchmark.recording.{IntervalRecorder, Recorder}

/**
  * Log the throughput measures of one ore multiple relations in a certain interval
  *
  * @param logId      Id of the log
  * @param evaluators Mapping of relation name that is used as identifier in the record book to the ThroughputEvaluator
  * @param transport
  */
class ThroughputRecorder(
							private val logId: String,
							private val evaluators: Map[String, ThroughputEvaluator[_]],
							private val transport: Transport[ThroughputFormat] = null
						) extends Recorder[ThroughputFormat]("throughput", logId, ThroughputFormat, transport)
	with IntervalRecorder[ThroughputFormat] {

	override protected val recording: Runnable = new Runnable {
		override def run(): Unit = {
			evaluators.foreach({
				case (relationName, evaluator) => {
					log(new ThroughputFormat(
						System.currentTimeMillis(),
						relationName,
						evaluator.timeSpan,
						evaluator.countEvaluator.eventCount,
						evaluator.countEvaluator.entryCount,
						evaluator.eventsPerSec
					))
				}
			})
		}
	}

	override def terminateAndTransfer(): Unit = {
		// Log the last value before terminating
		recording.run()
		super.terminateAndTransfer()
	}


}