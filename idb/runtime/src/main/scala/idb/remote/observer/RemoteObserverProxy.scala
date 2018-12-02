package idb.remote.observer

import akka.actor.ActorRef
import idb.observer.Observer
import idb.remote._

/**
  * Proxy of a remote observer, which acts as local observer on the side of the observable and forwards all incremental
  * events to the RemoteObserver on the receiving side.
  *
  * @param actorRef Reference to the parent RemoteObserver actor
  * @tparam V
  */
case class RemoteObserverProxy[-V](actorRef: ActorRef) extends Observer[V] with Serializable {
	override def added(v: V): Unit =
		actorRef ! Added(v)

	override def addedAll(vs: Seq[V]): Unit =
		actorRef ! AddedAll(vs)

	override def updated(oldV: V, newV: V): Unit =
		actorRef ! Updated(oldV, newV)

	override def removed(v: V): Unit =
		actorRef ! Removed(v)

	override def removedAll(vs: Seq[V]): Unit =
		actorRef ! RemovedAll(vs)
}