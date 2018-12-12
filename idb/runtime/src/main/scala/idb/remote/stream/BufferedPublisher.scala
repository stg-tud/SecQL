package idb.remote.stream

import java.util.concurrent.ConcurrentLinkedQueue

import idb.observer.{Observable, Observer}
import idb.remote._
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.mutable

case class BufferedPublisher[T](observable: Observable[T])
	extends Publisher[DataMessage[T]] with Observer[T] {
	observable.addObserver(this)

	private val subscriptions = mutable.HashMap.empty[Subscriber[_ >: DataMessage[T]], BufferedPublisherSubscription]

	override def subscribe(subscriber: Subscriber[_ >: DataMessage[T]]): Unit = {
		if (subscriptions.keySet.contains(subscriber))
			return

		val subscription = BufferedPublisherSubscription(subscriber)
		subscriptions += subscriber -> subscription
		subscriber.onSubscribe(subscription)
	}

	import scala.language.existentials
	case class BufferedPublisherSubscription(subscriber: Subscriber[_ >: DataMessage[T]]) extends Subscription {

		// Mutex lock for requested
		private val requestedRWLock = new Object

		private var requested: Long = 0

		val buffer: ConcurrentLinkedQueue[DataMessage[T]] = new ConcurrentLinkedQueue()

		override def request(n: Long): Unit = {
			// If Long overflows, set to max value
			requestedRWLock.synchronized {
				requested += n
				if (requested < 0) requested = Long.MaxValue
			}

			// Directly send messages from buffer
			trySend()
		}

		def trySend(): Unit = requestedRWLock.synchronized {
			while (requested > 0 && !buffer.isEmpty) {
				requested -= 1
				subscriber.onNext(buffer.poll())
			}
		}

		override def cancel(): Unit = {
			subscriptions.remove(subscriber)
			buffer.clear()
		}
	}

	override def added(v: T): Unit = publish(Added(v))

	override def addedAll(vs: Seq[T]): Unit = publish(AddedAll(vs))

	override def updated(oldV: T, newV: T): Unit = publish(Updated(oldV, newV))

	override def removed(v: T): Unit = publish(Removed(v))

	override def removedAll(vs: Seq[T]): Unit = publish(RemovedAll(vs))

	private def publish(msg: DataMessage[T]): Unit =
		subscriptions foreach {
			case (_, subscription) =>
				subscription.buffer.add(msg)
				subscription.trySend()
		}

}
