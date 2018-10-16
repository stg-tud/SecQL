package sae.benchmark.recording.impl

import sae.benchmark.recording.{Format, Recorder}

object PerformanceFormat extends Format[PerformanceFormat] {

	override def toLine(data: PerformanceFormat): String = {
		val sep = Recorder.SEPARATOR

		s"${data.time}$sep${data.cpuLoad}$sep${data.cpuTime}$sep${data.memory}"
	}

	override def fromLine(line: String): PerformanceFormat = {
		val record = line.split(Recorder.SEPARATOR)

		new PerformanceFormat(record(0).toLong, record(1).toDouble, record(2).toLong, record(3).toLong)
	}

}

class PerformanceFormat(
						   val time: Long,
						   val cpuLoad: Double,
						   val cpuTime: Long,
						   val memory: Long
					   ) {

}
