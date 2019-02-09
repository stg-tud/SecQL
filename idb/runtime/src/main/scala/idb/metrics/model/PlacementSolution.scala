package idb.metrics.model

/**
  * Represents a operator placement
  *
  * @param placements
  */
case class PlacementSolution(placements: Seq[PlacementDecision]) {

	def toString(linePrefix: String): String = {
		linePrefix + s"PlacementSolution([\n" +
			placements.map(_.toString(linePrefix + "	")).mkString(",\n") + "\n" +
			linePrefix + s"])"
	}

}