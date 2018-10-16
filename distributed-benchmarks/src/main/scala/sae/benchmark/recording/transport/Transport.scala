package sae.benchmark.recording.transport

abstract class Transport[T] {

	/**
	  * @param logId Identifier of the log
	  * @param logCategory Group name
	  * @param data Values
	  */
	def transfer(logId: String, logCategory: String, data: T)

}
