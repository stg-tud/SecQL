package sae.benchmark.recording.recorders

import idb.metrics.ProcessPerformance
import sae.benchmark.recording.{IntervalRecorder, Recorder, Transport}

/**
  * Logs performance metrics periodically
  *
  * @param executionId
  * @param nodeName
  * @param transport
  */
class PerformanceRecorder(
							 private val executionId: String,
							 private val nodeName: String,
							 private val transport: Transport[PerformanceRecord] = null
						 )
	extends Recorder[PerformanceRecord](executionId, "performance", nodeName, PerformanceRecord, transport)
		with IntervalRecorder[PerformanceRecord] {

	override protected val recording: RecordingRunnable = new RecordingRunnable {
		override protected def record(): Unit = {
			log(PerformanceRecord(
				nodeName,
				System.currentTimeMillis(),
				ProcessPerformance.cpuLoad(),
				ProcessPerformance.cpuTime(),
				ProcessPerformance.memory()
			))
		}
	}
}