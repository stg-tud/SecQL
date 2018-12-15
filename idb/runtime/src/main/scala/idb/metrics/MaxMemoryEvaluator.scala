package idb.metrics

import java.lang.management.ManagementFactory

import com.sun.management.GarbageCollectionNotificationInfo
import javax.management.{Notification, NotificationEmitter, NotificationFilter, NotificationListener}
import javax.management.openmbean.CompositeData

/**
  * Scan GC events in order to find the highest memory usage of the application directly after a GC
  */
class MaxMemoryEvaluator {

	import scala.collection.JavaConverters._

	private val gcEmitters = ManagementFactory.getGarbageCollectorMXBeans.asScala
		.map(_.asInstanceOf[NotificationEmitter])

	private var _maxMemory = 0L

	/**
	  * Maximal measured memory usage after a GC in bytes since initialization of the evaluator instance. Will not be
	  * updated anymore after evaluator instance was completed.
	  *
	  * @return
	  */
	def maxMemory: Long = _maxMemory

	/**
	  * Starts the measurement
	  */
	def start(): Unit =
		gcEmitters foreach {
			_.addNotificationListener(listener, notificationFilter, null)
		}

	/**
	  * Stops the measurement
	  */
	def stop(): Unit =
		gcEmitters foreach {
			_.removeNotificationListener(listener)
		}

	private val notificationFilter = new NotificationFilter {
		override def isNotificationEnabled(notification: Notification): Boolean =
			notification.getType == GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION
	}

	private val listener = new NotificationListener {
		override def handleNotification(notification: Notification, o: Any): Unit = {
			val info = GarbageCollectionNotificationInfo.from(notification.getUserData.asInstanceOf[CompositeData])
			val memoryUsage = info.getGcInfo.getMemoryUsageAfterGc.asScala.values.map(_.getUsed).sum

			synchronized {
				if (memoryUsage > maxMemory) _maxMemory = memoryUsage
			}
		}
	}
}
