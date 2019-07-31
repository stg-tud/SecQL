package sae.benchmark.recording.recorders

import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import sae.benchmark.recording.{Record, Recorder}
import sae.benchmark.recording.mongo.MongoRecord

object ConfigRecord extends Record[ConfigRecord] with MongoRecord[ConfigRecord] {

	override def toLine(data: ConfigRecord): String = {
		val sep = Recorder.SEPARATOR

		s"${data.node}$sep${data.typeName}$sep${data.property}$sep${data.value}"
	}

	override def fromLine(line: String): ConfigRecord = {
		val record = line.split(Recorder.SEPARATOR)

		ConfigRecord(record(0), record(1), record(2), record.slice(3, record.length).mkString(Recorder.SEPARATOR))
	}

	override val codecProvider: CodecProvider = classOf[ConfigRecord]

}

case class ConfigRecord(node: String, typeName: String, property: String, value: String)