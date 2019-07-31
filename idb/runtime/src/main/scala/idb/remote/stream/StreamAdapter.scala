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
	def bufferFromObservable[T](observable: Observable[T], onObserved: () => Unit = null): mutable.Queue[DataMessage[T]] = {
		val buffer = new mutable.Queue[DataMessage[T]]()

		val bridge = new BridgeObserver[T](
			msg => {
				buffer.enqueue(msg)
				if (onObserved != null)
					onObserved()
			})
		observable.addObserver(bridge)

		buffer
	}

	def wrapLinearOperatorTree[T, U](leaf: Observer[T], root: Relation[U]): DataMessage[T] => immutable.Iterable[DataMessage[U]] = {
		val bufferedResults = StreamAdapter.bufferFromObservable(root)

		msg: DataMessage[T] => {
			StreamAdapter.toObserver(msg, leaf)
			immutable.Iterable(bufferedResults.dequeueAll(_ => true): _*)
		}
	}

}
