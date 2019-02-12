package idb.metrics.model

/**
  * Represents an operator in a placement problem
  *
  * @param id             ID
  * @param operatorName   Description of the operator
  * @param hostCandidates IDs of the hosts, the operator can be placed on
  * @param selectivity    Selectivity of the operator
  * @param load           Load caused by this operator
  * @param outgoingLink   Outgoing link strength
  */
case class PlacementOperator(id: Int,
							 operatorName: String,
							 hostCandidates: Seq[Int],
							 selectivity: Double,
							 load: Double,
							 outgoingLink: Double) {

	def toString(linePrefix: String): String = {
		linePrefix + s"PlacementOperator(\n" +
			linePrefix + s"	id: $id,\n" +
			linePrefix + s"	operator: $operatorName,\n" +
			linePrefix + s"	host candidates: $hostCandidates,\n" +
			linePrefix + s"	selectivity: $selectivity,\n" +
			linePrefix + s"	load: $load,\n" +
			linePrefix + s"	outgoing link: $outgoingLink,\n" +
			linePrefix + s")"
	}

}