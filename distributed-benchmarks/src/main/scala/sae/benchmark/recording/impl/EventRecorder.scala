package sae.benchmark.recording.impl

import sae.benchmark.recording.Recorder
import sae.benchmark.recording.transport.Transport

/**
  * Records simple event tags with timestamp
  *
  * @param logId
  * @param transport
  */
class EventRecorder(
					   private val logId: String,
					   private val transport: Transport[EventFormat] = null
				   ) extends Recorder[EventFormat]("event", logId, EventFormat, transport) {

	def log(event: String): Unit = {
		log(new EventFormat(
			System.currentTimeMillis(),
			event
		))
	}

}