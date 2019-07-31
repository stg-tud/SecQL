package idb.observer

/**
  * This trait implements an observer that does nothing. It is meant for convenience, since it's extensions do not need
  * to define methods for events, which are irrelevant in their case.
  *
  * @tparam
  */
trait NoOpObserver[-V] extends Observer[V] {
	def added(v: V) = {}

	def addedAll(vs: Seq[V]) = {}

	def updated(oldV: V, newV: V) = {}

	def removed(v: V) = {}

	def removedAll(vs: Seq[V]) = {}
}