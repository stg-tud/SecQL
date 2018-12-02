package idb.remote.observer

import akka.actor.ActorRef
import idb.observer.{Observable, Observer}

case class RemoteObservableProxy[+V](actorRef: ActorRef) extends Observable[V] {

	override def addObserver[U >: V](o: Observer[U]): Unit =
		actorRef ! AddObserver(o.asInstanceOf[RemoteObserverProxy[U]])

	override def removeObserver[U >: V](o: Observer[U]): Unit =
		actorRef ! RemoveObserver(o.asInstanceOf[RemoteObserverProxy[U]])

	override def clearObservers(): Unit =
		actorRef ! ClearObservers

	/**
	  * Returns the observed children, to allow a top down removal of observers
	  */
	override def children: Seq[Observable[_]] = Nil

}