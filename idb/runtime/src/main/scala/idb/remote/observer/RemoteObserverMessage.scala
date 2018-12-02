package idb.remote.observer

sealed trait RemoteObserverMessage extends Serializable

case class AddObserver[U](o: RemoteObserverProxy[U]) extends RemoteObserverMessage
case class RemoveObserver[U](o: RemoteObserverProxy[U]) extends RemoteObserverMessage
case object ClearObservers extends RemoteObserverMessage