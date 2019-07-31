package sae.benchmark.db

class SimpleBenchmarkDB[T](config: BenchmarkDBConfig[T]) extends BenchmarkDB[T](config) {

	private var initIterationNo = 0
	private var iterationNo = 0

	def completedInit: Boolean = initIterationNo >= config.initIterations

	def completed: Boolean = iterationNo >= config.iterations

	/**
	  *
	  * @return Returns false, if no further iterations planed
	  */
	def baseInitIteration(): Boolean = {
		0 until config.initIterationSpeed foreach { _ =>
			initIteration()
		}
		!completedInit
	}

	/**
	  *
	  * @return Returns false, if no further iterations planed
	  */
	def baseIteration(): Boolean = {
		0 until config.iterationSpeed foreach { _ =>
			iteration()
		}
		!completed
	}

	def initIteration(): Unit =
		if (!completedInit) {
			config.initIteration(this, initIterationNo)
			initIterationNo += 1
		}

	def iteration(): Unit =
		if (!completed) {
			config.iteration(this, iterationNo)
			iterationNo += 1
		}

	override def reset(): Unit = {
		super.reset()
		initIterationNo = 0
		iterationNo = 0
	}
}