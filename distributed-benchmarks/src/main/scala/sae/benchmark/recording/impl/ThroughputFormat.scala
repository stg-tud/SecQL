package sae.benchmark.recording.impl

import sae.benchmark.recording.{Format, Recorder}

object ThroughputFormat extends Format[ThroughputFormat] {

	override def toLine(data: ThroughputFormat): String = {
		val sep = Recorder.SEPARATOR

		s"${data.time}$sep${data.relationName}$sep${data.timeSpan}$sep${data.eventCount}$sep${data.entryCount}$sep${data.eventsPerSec}"
	}

	override def fromLine(line: String): ThroughputFormat = {
		val record = line.split(Recorder.SEPARATOR)

		new ThroughputFormat(record(0).toLong, record(1), record(2).toLong, record(3).toLong, record(4).toLong, record(5).toDouble)
	}

}

class ThroughputFormat(
						  val time: Long,
						  val relationName: String,
						  val timeSpan: Long,
						  val eventCount: Long,
						  val entryCount: Long,
						  val eventsPerSec: Double
					  ) {
}