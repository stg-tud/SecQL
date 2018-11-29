package sae.benchmark.recording

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

/**
  * Abstract metric recorder, that logs in a defined interval
  *
  * @tparam T
  */
trait IntervalRecorder[T] extends Recorder[T] {

	abstract class RecordingRunnable extends Runnable {
		private val hasStartedPromise = Promise[Unit]()

		val hasStarted: Future[Unit] = hasStartedPromise.future

		protected def record(): Unit

		override def run(): Unit = {
			record()
			if (!hasStartedPromise.isCompleted)
				hasStartedPromise.success()
		}
	}

	protected val recording: RecordingRunnable

	private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
	private var measurementFuture: ScheduledFuture[_] = _

	/**
	  * Starts the interval recording and returns synchronously after first record has been written in order to make
	  * sure, scheduling started
	  *
	  * @param interval
	  */
	def start(interval: Long): Unit = {
		if (measurementFuture != null && !measurementFuture.isDone) {
			throw new Exception("Interval logging already started")
		}

		measurementFuture = executor
			.scheduleAtFixedRate(recording, 0, interval, TimeUnit.MILLISECONDS)

		Await.result(recording.hasStarted, Duration.Inf)
	}

	def stop(): Unit = {
		measurementFuture.cancel(false)
	}

	override def terminateAndTransfer(): Unit = {
		stop()
		executor.shutdown()

		super.terminateAndTransfer()
	}
}