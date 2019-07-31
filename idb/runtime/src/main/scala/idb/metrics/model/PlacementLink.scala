package idb.metrics.model

/**
  * Represents a link on a placement problem
  *
  * @param sender   Id of sending operator
  * @param receiver Id of receiving operator
  * @param load     Load on the link
  */
case class PlacementLink(sender: Int, receiver: Int, load: Double) {

	def toString(linePrefix: String): String = {
		linePrefix + s"PlacementLink(\n" +
			linePrefix + s"	sender: $sender,\n" +
			linePrefix + s"	receiver: $receiver,\n" +
			linePrefix + s"	load: $load,\n" +
			linePrefix + s")"
	}

}
