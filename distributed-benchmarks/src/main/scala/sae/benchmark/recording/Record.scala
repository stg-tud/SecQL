package sae.benchmark.recording

/**
  * Recording format
  *
  * @tparam T
  */
trait Record[T] {

	def toLine(data: T): String

	def fromLine(line: String): T

}