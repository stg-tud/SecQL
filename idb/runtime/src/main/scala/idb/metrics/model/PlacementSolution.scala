package idb.metrics.model

/**
  * Represents a operator placement
  *
  * @param placements Operator id => host id
  */
case class PlacementSolution(placements: Map[Int, Int]) {

	def toString(linePrefix: String): String = {
		linePrefix + s"PlacementSolution([\n" +
			placements.map(t => linePrefix + s"	operator ${t._1} => host ${t._2}").mkString(",\n") + "\n" +
			linePrefix + s"])"
	}

}