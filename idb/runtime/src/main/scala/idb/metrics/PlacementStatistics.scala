package idb.metrics

import idb.metrics.model.{PlacementHost, PlacementLink, PlacementOperator, PlacementSolution}

import scala.collection.mutable

object PlacementStatistics {

	private val statistics = new mutable.HashMap[String, PlacementStatistics]()

	/**
	  * Return placement statistics for a given placement id from the central placement statistics storage
	  *
	  * @param placementId
	  * @return Placement Statistics of the placement id (only available after placement decision was completed)
	  */
	def apply(placementId: String): Option[PlacementStatistics] = statistics.get(placementId)

	/**
	  * Create placement statistics and make it available in the central placement statistics storage
	  *
	  * @param placementId
	  * @param cost             Cost value of the best solutions
	  * @param solutions        Sequence of all found best solutions
	  * @param selectedSolution Id of the solution in solutions, which was chosen for execution
	  * @param operators        Operators
	  * @param links            Links on the operator tree
	  * @param hosts            Host descriptions
	  * @param duration         Total duration of the search in ms
	  * @param timedOut         True, if the search timed out, false if it was not terminated prematurely
	  * @return
	  */
	def apply(placementId: String,
			  cost: Double,
			  solutions: Seq[PlacementSolution],
			  selectedSolution: Int,
			  operators: Seq[PlacementOperator],
			  links: Seq[PlacementLink],
			  hosts: Seq[PlacementHost],
			  duration: Long,
			  timedOut: Boolean): PlacementStatistics = {
		val stats = PlacementStatistics(cost, solutions, selectedSolution, operators, links, hosts, duration, timedOut)
		statistics.put(placementId, stats)
		stats
	}

}

/**
  * Statistical information about a placement decision
  *
  * @param cost             Cost value of the best solutions
  * @param solutions        Sequence of all found best solutions
  * @param selectedSolution Id of the solution in solutions, which was chosen for execution
  * @param operators        Operators
  * @param links            Links on the operator tree
  * @param hosts            Host descriptions
  * @param duration         Total duration of the search in ms
  * @param timedOut         True, if the search timed out, false if it was not terminated prematurely
  */
case class PlacementStatistics(cost: Double,
							   solutions: Seq[PlacementSolution],
							   selectedSolution: Int,
							   operators: Seq[PlacementOperator],
							   links: Seq[PlacementLink],
							   hosts: Seq[PlacementHost],
							   duration: Long,
							   timedOut: Boolean) {
	override def toString: String = {
		s"PlacementStatistics(\n" +
			s"	cost: $cost,\n" +
			s"	duration [ms]: $duration,\n" +
			s"	timed out: $timedOut,\n" +
			s"	operators: [\n" + operators.map(_.toString("		")).mkString(",\n") + "\n" +
			s"	],\n" +
			s"	links: [\n" + links.map(_.toString("		")).mkString(",\n") + "\n" +
			s"	],\n" +
			s"	hosts: [\n" + hosts.map(_.toString("		")).mkString(",\n") + "\n" +
			s"	],\n" +
			s"	solutions: [\n" + solutions.map(_.toString("		")).mkString(",\n") + "\n" +
			s"	],\n" +
			s"	final solution: $selectedSolution\n)"
	}
}