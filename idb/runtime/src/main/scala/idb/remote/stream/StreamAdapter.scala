package idb.remote.stream

import idb.Relation
import idb.observer.{Observable, Observer}
import idb.remote._

import scala.collection.{immutable, mutable}

object StreamAdapter {

	def toObserver[T](msg: DataMessage[T], observer: Observer[T]): Unit = {
		msg match {
			case m: Added[T] => observer.added(m.d)
			case m: AddedAll[T] => observer.addedAll(m.d)
			case m: Updated[T] => observer.updated(m.oldV, m.newV)
			case m: Removed[T] => observer.removed(m.d)
			case m: RemovedAll[T] => observer.removedAll(m.d)
		}
	}

	/**
	  *
	  * @param observable
	  * @param onObserved Optional callback method, that is called, whenever a new message was observed
	  * @tparam T
	  * @return
	  */
	def fromObservable[T](observable: Observable[T], onObserved: () => Unit = null): mutable.Queue[DataMessage[T]] = {
		val buffer = new mutable.Queue[DataMessage[T]]()

		observable.addObserver(new BridgeObserver())

		class BridgeObserver extends Observer[T] {
			private def enqueue(msg: DataMessage[T]): Unit = {
				buffer.enqueue(msg)
				if (onObserved != null) {
					onObserved()
				}
			}

			override def added(v: T): Unit = enqueue(Added(v))

			override def addedAll(vs: Seq[T]): Unit = enqueue(AddedAll(vs))

			override def updated(oldV: T, newV: T): Unit = enqueue(Updated(oldV, newV))

			override def removed(v: T): Unit = enqueue(Removed(v))

			override def removedAll(vs: Seq[T]): Unit = enqueue(RemovedAll(vs))
		}

		buffer
	}


	def wrapLinearOperatorTree[T, U](leaf: Observer[T], root: Relation[U]): DataMessage[T] => immutable.Iterable[DataMessage[U]] = {
		val bufferedResults = StreamAdapter.fromObservable(root)

		msg: DataMessage[T] => {
			StreamAdapter.toObserver(msg, leaf)
			immutable.Iterable(bufferedResults.dequeueAll(_ => true): _*)
		}
	}

}
