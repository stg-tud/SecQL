package sae.benchmark.recording.recorders

import sae.benchmark.recording.{Recorder, Transport}

/**
  * Records simple event tags with timestamp
  *
  * @param executionId
  * @param nodeName
  * @param transport
  */
class EventRecorder(
					   private val executionId: String,
					   private val nodeName: String,
					   private val transport: Transport[EventRecord] = null
				   ) extends Recorder[EventRecord](executionId, "event", nodeName, EventRecord, transport) {

	def log(event: String): Unit = {
		log(new EventRecord(
			nodeName,
			System.currentTimeMillis(),
			event
		))
	}

}