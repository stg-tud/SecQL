package sae.benchmark.recording.transport

class MongoTransport[T](
					   connectionString: String,
					   nodeName: String
					   ) extends Transport[T] {

	/**
	  * @param logId       Identifier of the log
	  * @param logCategory Group name
	  * @param data        Values
	  */
	override def transfer(logId: String, logCategory: String, data: T): Unit = ???

}
