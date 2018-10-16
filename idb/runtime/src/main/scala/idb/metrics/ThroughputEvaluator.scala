package idb.metrics

import idb.Relation
import idb.observer.Observer

/**
  * Evaluates the throughput of a relation by resulting total receiving time span and number of events per second
  *
  * @param relation       The relation to evaluate
  * @param countEvaluator The CountEvaluator instance to use, which should observe the same relation. Created if not set
  * @tparam Domain
  */
class ThroughputEvaluator[Domain](
									 val relation: Relation[Domain],
									 var countEvaluator: CountEvaluator[Domain] = null
								 ) extends Observer[Domain] {

	relation.addObserver(this)
	if (countEvaluator == null)
		countEvaluator = new CountEvaluator[Domain](relation)

	private var firstEventTime: Long = -1
	private var _lastEventTime: Long = -1

	private def handleEvent(): Unit = {
		val currTime = System.currentTimeMillis()
		if (firstEventTime == -1)
			firstEventTime = currTime

		_lastEventTime = currTime
	}

	def lastEventTime: Long = _lastEventTime

	/**
	  * @return Time span between first and last event in milliseconds
	  */
	def timeSpan: Long = if (firstEventTime != _lastEventTime) _lastEventTime - firstEventTime else 0

	/**
	  * @return Average of number of events per seconds over the measured time span
	  */
	def eventsPerSec: Double = (countEvaluator.eventCount * 1000).toDouble / timeSpan

	def updated(oldV: Domain, newV: Domain): Unit = {
		handleEvent()
	}

	def removed(v: Domain): Unit = {
		handleEvent()
	}

	def removedAll(vs: Seq[Domain]): Unit = {
		handleEvent()
	}

	def added(v: Domain): Unit = {
		handleEvent()
	}

	def addedAll(vs: Seq[Domain]): Unit = {
		handleEvent()
	}

}