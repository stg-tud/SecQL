package sae.benchmark.recording.recorders

import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import sae.benchmark.recording.mongo.MongoRecord
import sae.benchmark.recording.{Record, Recorder}

object PerformanceRecord extends Record[PerformanceRecord] with MongoRecord[PerformanceRecord] {

	override def toLine(data: PerformanceRecord): String = {
		val sep = Recorder.SEPARATOR

		s"${data.node}$sep${data.time}$sep${data.cpuLoad}$sep${data.cpuTime}$sep${data.memory}"
	}

	override def fromLine(line: String): PerformanceRecord = {
		val record = line.split(Recorder.SEPARATOR)

		PerformanceRecord(record(0), record(1).toLong, record(2).toDouble, record(3).toLong, record(4).toLong)
	}

	override val codecProvider: CodecProvider = classOf[PerformanceRecord]

}

case class PerformanceRecord(node: String, time: Long, cpuLoad: Double, cpuTime: Long, memory: Long)