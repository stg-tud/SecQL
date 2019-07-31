package idb.metrics

import idb.Relation
import idb.observer.Observer

/**
  * Evaluates the number of events and number of entities at a relation
  *
  * @param relation The relation to evaluate
  * @tparam Domain
  */
class CountEvaluator[Domain](
								val relation: Relation[Domain]
							) extends Observer[Domain] {
	relation.addObserver(this)

	private var events: Long = 0
	private var entries: Long = 0

	/**
	  * @return Number of events that happened on this relation
	  */
	def eventCount: Long = events

	/**
	  * @return Number of entries in this relation
	  */
	def entryCount: Long = entries

	override def updated(oldV: Domain, newV: Domain): Unit = {
		events = events + 1
	}

	override def removed(v: Domain): Unit = {
		events = events + 1
		entries = entries - 1
	}

	override def removedAll(vs: Seq[Domain]): Unit = {
		events = events + vs.size
		entries = entries - vs.size
	}

	override def added(v: Domain): Unit = {
		events = events + 1
		entries = entries + 1
	}

	override def addedAll(vs: Seq[Domain]): Unit = {
		events = events + vs.size
		entries = entries + vs.size
	}
}