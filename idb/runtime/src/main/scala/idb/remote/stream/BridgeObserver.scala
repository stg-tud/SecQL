package idb.remote.stream

import idb.observer.Observer
import idb.remote._


class BridgeObserver[T](onNext: DataMessage[T] => Unit) extends Observer[T] {

	override def added(v: T): Unit = onNext(Added(v))

	override def addedAll(vs: Seq[T]): Unit = onNext(AddedAll(vs))

	override def updated(oldV: T, newV: T): Unit = onNext(Updated(oldV, newV))

	override def removed(v: T): Unit = onNext(Removed(v))

	override def removedAll(vs: Seq[T]): Unit = onNext(RemovedAll(vs))

}