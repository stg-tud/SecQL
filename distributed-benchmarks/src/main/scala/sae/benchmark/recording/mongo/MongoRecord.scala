package sae.benchmark.recording.mongo

import org.bson.codecs.configuration.CodecProvider

trait MongoRecord[T] {

	/**
	  * BSON Codec for the record's case class
	  */
	val codecProvider: CodecProvider

}
