package sae.benchmark.recording

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

/**
  * Abstract metric recorder, that logs in a defined interval
  *
  * @tparam T
  */
trait IntervalRecorder[T] extends Recorder[T] {

	protected val recording: Runnable

	private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
	private var measurementFuture: ScheduledFuture[_] = _

	def start(interval: Long): Unit = {
		if (measurementFuture != null && !measurementFuture.isDone) {
			throw new Exception("Interval logging already started")
		}

		measurementFuture = executor
			.scheduleAtFixedRate(recording, 0, interval, TimeUnit.MILLISECONDS)
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