package sae.benchmark.recording

/**
  * Recording format
  *
  * @tparam Type
  */
trait Format[Type] {

	def toLine(data: Type): String

	def fromLine(line: String): Type

}