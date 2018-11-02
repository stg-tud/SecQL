package sae.benchmark.recording.recorders

import idb.metrics.ThroughputEvaluator
import sae.benchmark.recording.{IntervalRecorder, Recorder, Transport}

/**
  * Log the throughput measures of one ore multiple relations in a certain interval
  *
  * @param executionId
  * @param nodeName
  * @param evaluators Mapping of relation name that is used as identifier in the record book to the ThroughputEvaluator
  * @param transport
  */
class ThroughputRecorder(
							private val executionId: String,
							private val nodeName: String,
							private val evaluators: Map[String, ThroughputEvaluator[_]],
							private val transport: Transport[ThroughputRecord] = null
						) extends Recorder[ThroughputRecord](executionId, "throughput", nodeName, ThroughputRecord, transport)
	with IntervalRecorder[ThroughputRecord] {

	override protected val recording: Runnable = new Runnable {
		override def run(): Unit = {
			evaluators.foreach({
				case (relationName, evaluator) => {
					log(new ThroughputRecord(
						nodeName,
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