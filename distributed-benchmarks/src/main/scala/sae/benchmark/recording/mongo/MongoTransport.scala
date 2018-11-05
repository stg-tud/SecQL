package sae.benchmark.recording.mongo

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{MongoClient, MongoCollection}
import sae.benchmark.recording.Transport
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

class MongoTransport[T](
						   private val connectionString: String,
						   private val recordCompanion: MongoRecord[T]
					   )(implicit ct: ClassTag[T]) extends Transport[T] {

	private var mongoClient: MongoClient = _
	private var mongoCollection: MongoCollection[T] = _


	/**
	  * Initialize and open transport channel
	  *
	  * @param executionId Identifier of the execution
	  * @param logType     Type of the log information
	  */
	override def open(executionId: String, logType: String): Unit = {
		val codecRegistry = fromRegistries(fromProviders(recordCompanion.codecProvider), DEFAULT_CODEC_REGISTRY)

		mongoClient = MongoClient(connectionString)
		mongoCollection = mongoClient
			.getDatabase("i3ql-benchmarks")
			.getCollection[T](s"$executionId.$logType")
			.withCodecRegistry(codecRegistry)
	}

	override def close(): Unit = {
		mongoClient.close()
	}

	override def transfer(data: T): Unit = {
		val operation = mongoCollection.insertOne(data)
		Await.result(operation.toFuture(), Duration.Inf)
	}

}
