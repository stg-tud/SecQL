package sae.benchmark.recording

trait Transport[T] {

	/**
	  * Initialize and open transport channel
	  *
	  * @param executionId Identifier of the execution
	  * @param logType     Type of the log information
	  */
	def open(executionId: String, logType: String)

	/**
	  * Close transport channel
	  *
	  * @return
	  */
	def close()

	/**
	  * @param data Values
	  */
	def transfer(data: T)

}
