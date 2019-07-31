package sae.benchmark.recording.recorders

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import sae.benchmark.recording.mongo.MongoRecord
import sae.benchmark.recording.{Record, Recorder}

object ThroughputRecord extends Record[ThroughputRecord] with MongoRecord[ThroughputRecord] {

	override def toLine(data: ThroughputRecord): String = {
		val sep = Recorder.SEPARATOR

		s"${data.node}$sep${data.time}$sep${data.relationName}$sep${data.timeSpan}$sep${data.eventCount}$sep${data.entryCount}$sep${data.eventsPerSec}"
	}

	override def fromLine(line: String): ThroughputRecord = {
		val record = line.split(Recorder.SEPARATOR)

		ThroughputRecord(record(0), record(1).toLong, record(2), record(3).toLong, record(4).toLong, record(5).toLong, record(6).toDouble)
	}

	override val codecProvider: CodecProvider = classOf[ThroughputRecord]

}

case class ThroughputRecord(node: String, time: Long, relationName: String, timeSpan: Long, eventCount: Long, entryCount: Long, eventsPerSec: Double)