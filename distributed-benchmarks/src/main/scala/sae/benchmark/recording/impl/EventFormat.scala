package sae.benchmark.recording.impl

import sae.benchmark.recording.{Format, Recorder}

object EventFormat extends Format[EventFormat] {

	override def toLine(data: EventFormat): String = {
		val sep = Recorder.SEPARATOR

		s"${data.time}$sep${data.event}"
	}

	override def fromLine(line: String): EventFormat = {
		val record = line.split(Recorder.SEPARATOR)

		new EventFormat(record(0).toLong, record(1))
	}

}

class EventFormat(
					 val time: Long,
					 val event: String
				 ) {
}