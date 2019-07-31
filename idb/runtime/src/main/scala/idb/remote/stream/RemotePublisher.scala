package idb.remote.stream

import java.util.concurrent.ConcurrentLinkedQueue

import idb.remote._
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.mutable

/**
  * Back-pressure capable publisher of DataMessages for remote connections. The fastest subscriber controls the speed
  * of the production. Events produced, but not yet request by some of the publishers get buffered. Therefore, in case
  * multiple consumers are subscribed, their demand speed should be similar, in case of faster production.
  *
  * @tparam T
  */
trait RemotePublisher[T] extends Publisher[DataMessage[T]] {

	private val subscriptions = mutable.HashMap.empty[Subscriber[_ >: DataMessage[T]], RemotePublisherSubscription]

	def hasSubscribers: Boolean = subscriptions.nonEmpty

	override def subscribe(subscriber: Subscriber[_ >: DataMessage[T]]): Unit = {
		if (subscriptions.keySet.contains(subscriber))
			return

		val subscription = RemotePublisherSubscription(subscriber)
		subscriptions += subscriber -> subscription
		subscriber.onSubscribe(subscription)
	}

	/**
	  * Called whenever demand increased
	  */
	protected def produce(): Unit

	protected def publish(msg: DataMessage[T]): Unit =
		subscriptions foreach { case (_, subscription) => subscription.publish(msg) }

	protected def publishAll(msgs: Seq[DataMessage[T]]): Unit =
		subscriptions foreach { case (_, subscription) => subscription.publishAll(msgs) }

	protected def demand = if (subscriptions.isEmpty) 0L else subscriptions.values.map(_.requested).max

	import scala.language.existentials

	case class RemotePublisherSubscription(subscriber: Subscriber[_ >: DataMessage[T]]) extends Subscription {
		private val buffer: ConcurrentLinkedQueue[DataMessage[T]] = new ConcurrentLinkedQueue()

		// Mutex lock for requested
		private val requestedRWLock = new Object

		private var _requested: Long = 0

		def requested: Long = _requested

		def pending: Int = buffer.size()

		override def request(n: Long): Unit = {
			// If Long overflows, set to max value
			requestedRWLock.synchronized {
				_requested += n
				if (_requested < 0) _requested = Long.MaxValue
			}

			trySend()
			if (demand > 0)
				produce()
		}

		def publish(msg: DataMessage[T]): Unit = {
			buffer.add(msg)
			trySend()
		}

		def publishAll(msgs: Seq[DataMessage[T]]): Unit = {
			buffer.addAll(scala.collection.JavaConversions.seqAsJavaList(msgs))
			trySend()
		}

		def trySend(): Unit = requestedRWLock.synchronized {
			while (requested > 0 && !buffer.isEmpty) {
				_requested -= 1
				subscriber.onNext(buffer.poll())
			}
		}

		override def cancel(): Unit = {
			subscriptions.remove(subscriber)
			buffer.clear()
		}
	}

}