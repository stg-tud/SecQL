package sae.benchmark.recording

import java.io._

object Recorder {
	val SEPARATOR = ","
}

/**
  * A recorder is an abstract metric logging interface for the benchmark implementations
  *
  * @param logType
  * @param format
  * @param transport
  * @tparam T
  */
class Recorder[T](
					 private val executionId: String,
					 private val logType: String,
					 private val nodeName: String,
					 private val format: Record[T],
					 private val transport: Transport[T]
				 ) {

	private val path = "log/distributed-benchmarks"
	private val dir = new File(path)
	if (!dir.exists()) dir.mkdirs()

	private val file = new File(s"$path/$executionId.$logType.$nodeName.log")
	file.deleteOnExit()
	private val writer = new PrintWriter(file)

	def log(data: T): Unit = {
		writer.println(format.toLine(data))
	}

	/**
	  * Terminates recording and transfers all records. Optionally, a manipulation function can be provided in the
	  * property `transferManipulation`, which is called for each record. It's returned sequence of records is
	  * transferred instead of the original record.
	  */
	def terminateAndTransfer(): Unit = {
		writer.close()

		if (transport != null) {
			val fileReader = new FileReader(file)
			val reader = new BufferedReader(fileReader)

			transport.open(executionId, logType)

			var line: String = null
			while ( {
				line = reader.readLine()
				line != null
			}) {
				val data: T = format.fromLine(line)

				if (transferManipulation == null)
					transport.transfer(data)
				else
					transferManipulation(data) foreach (record =>
						transport.transfer(record))
			}

			transport.close()
		}
	}

	var transferManipulation: (T) => Seq[T] = null
}