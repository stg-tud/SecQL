package idb.remote.observer

import akka.actor.Actor
import idb.observer.Observable

/**
  * Wraps a regular local Observable with a remote interface
  *
  * @param observable
  * @tparam V
  */
case class RemoteObservableAdapter[+V](observable: Observable[V]) extends Actor {

	override def receive: Receive = {
		case AddObserver(o: RemoteObserverProxy[V]) =>
			observable.addObserver(o)
		case RemoveObserver(o: RemoteObserverProxy[V]) =>
			observable.removeObserver(o)
		case ClearObservers =>
			observable.clearObservers()
	}

}