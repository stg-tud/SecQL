package sae.benchmark.db

import idb.remote.stream.{BridgeObserver, RemotePublisher}
import scala.concurrent.{Future, Promise}

class PublisherBenchmarkDB[T](
								 config: BenchmarkDBConfig[T]
							 ) extends BenchmarkDB[T](config) with RemotePublisher[T] {
	private val bridge = new BridgeObserver[T](publish)
	addObserver(bridge)

	private val totalIterations = config.initIterations + config.iterations
	private var iterationNo = 0

	private var initStarted = false
	private var initCompleted: Promise[Unit] = Promise()
	private var measurementStarted = false
	private var measurementCompleted: Promise[Unit] = Promise()

	/**
	  *
	  * @return Future that resolves, once all init iterations have been executed
	  */
	def execInit(): Future[Unit] = {
		iterationNo = 0
		initStarted = true
		produce()
		initCompleted.future
	}

	/**
	  *
	  * @return Future that resolves, once all iterations have been executed
	  */
	def execMeasurement(): Future[Unit] = {
		iterationNo = config.initIterations
		measurementStarted = true
		produce()
		measurementCompleted.future
	}

	/**
	  * Called whenever demand increased
	  */
	override protected def produce(): Unit = this.synchronized {
		while (demand > 0 && hasNext) next()
	}

	def initMode: Boolean = iterationNo < config.initIterations

	def completed: Boolean = iterationNo >= totalIterations

	protected def hasNext: Boolean = {
		if (completed) {
			initCompleted.trySuccess()
			measurementCompleted.trySuccess()
			return false
		}
		else if (iterationNo == config.initIterations) {
			// measurementStarted before initStarted, for the case that initIterations = 0
			initCompleted.trySuccess()
			if (!measurementStarted)
				return false
		}
		else if (iterationNo == 0 && !initStarted)
			return false

		true
	}

	protected def next(): Unit = {
		if (initMode)
			config.initIteration(this, iterationNo)
		else
			config.iteration(this, iterationNo - config.initIterations)
		iterationNo += 1
	}

	override def reset(): Unit = {
		super.reset()
		iterationNo = 0
		initStarted = false
		initCompleted = Promise()
		measurementStarted = false
		measurementCompleted = Promise()
	}
}