package sae.benchmark.recording.impl

import idb.metrics.ProcessPerformance
import sae.benchmark.recording.transport.Transport
import sae.benchmark.recording.{IntervalRecorder, Recorder}

/**
  * Logs performance metrics periodically
  *
  * @param logId
  * @param transport
  */
class PerformanceRecorder(
							 private val logId: String,
							 private val transport: Transport[PerformanceFormat] = null
						 )
	extends Recorder[PerformanceFormat]("performance", logId, PerformanceFormat, transport)
		with IntervalRecorder[PerformanceFormat] {

	override protected val recording: Runnable = new Runnable {
		override def run(): Unit = {
			log(new PerformanceFormat(
				System.currentTimeMillis(),
				ProcessPerformance.cpuLoad(),
				ProcessPerformance.cpuTime(),
				ProcessPerformance.memory()
			))
		}
	}
}