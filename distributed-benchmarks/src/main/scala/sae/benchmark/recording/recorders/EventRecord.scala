package sae.benchmark.recording.recorders

import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import sae.benchmark.recording.mongo.MongoRecord
import sae.benchmark.recording.{Record, Recorder}

object EventRecord extends Record[EventRecord] with MongoRecord[EventRecord] {

	override def toLine(data: EventRecord): String = {
		val sep = Recorder.SEPARATOR

		s"${data.node}$sep${data.time}$sep${data.event}"
	}

	override def fromLine(line: String): EventRecord = {
		val record = line.split(Recorder.SEPARATOR)

		EventRecord(record(0), record(1).toLong, record(2))
	}

	override val codecProvider: CodecProvider = classOf[EventRecord]

}

case class EventRecord(node: String, time: Long, event: String)