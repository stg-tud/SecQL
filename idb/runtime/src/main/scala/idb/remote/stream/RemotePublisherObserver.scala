package idb.remote.stream

import idb.observer.{Observable, Observer}
import idb.remote._

case class RemotePublisherObserver[T](observable: Observable[T])
	extends RemotePublisher[T] with Observer[T] {

	observable.addObserver(this)

	/**
	  * Called whenever demand increased
	  *
	  * Ignore it, since the production speed of the observable cannot be influenced from here
	  */
	override protected def produce(): Unit = Unit

	override def added(v: T): Unit = publish(Added(v))

	override def addedAll(vs: Seq[T]): Unit = publish(AddedAll(vs))

	override def updated(oldV: T, newV: T): Unit = publish(Updated(oldV, newV))

	override def removed(v: T): Unit = publish(Removed(v))

	override def removedAll(vs: Seq[T]): Unit = publish(RemovedAll(vs))
}