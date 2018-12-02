package idb.remote.observer

import akka.actor.Actor
import idb.observer.Observer
import idb.remote.Added
import idb.remote._

/**
  * Observer, which can observe data from remote by registering a related RemoteObserverProxy on the RemoteObservable
  * side.
  *
  * @tparam V
  */
trait RemoteObserver[-V] extends Actor with Observer[V] {

	override def receive: Receive = {
		case Added(v: V) =>
			added(v)
		case AddedAll(vs: Seq[V]) =>
			addedAll(vs)
		case Removed(v: V) =>
			removed(v)
		case Updated(oldV: V, newV: V) =>
			updated(oldV, newV)
		case RemovedAll(vs: Seq[V]) =>
			removedAll(vs)
	}

}