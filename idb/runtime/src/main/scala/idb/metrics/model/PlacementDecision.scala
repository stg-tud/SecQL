package idb.metrics.model

/**
  * Represents a decision in a PlacementSolution
  *
  * @param operator Operator ID
  * @param host     ID of the host
  */
case class PlacementDecision(operator: Int, host: Int) {

	def toString(linePrefix: String): String = {
		linePrefix + s"PlacementDecision(operator $operator => host $host)"
	}

}