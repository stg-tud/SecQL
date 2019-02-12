package idb.metrics.model

/**
  * Represents a host in a placement problem
  *
  * @param id   ID
  * @param name Name of the host
  */
case class PlacementHost(id: Int, name: String) {

	def toString(linePrefix: String): String = {
		linePrefix + s"PlacementHostOperator(\n" +
			linePrefix + s"	id: $id,\n" +
			linePrefix + s"	name: $name,\n" +
			linePrefix + s")"
	}

}
