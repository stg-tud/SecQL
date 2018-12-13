package sae.benchmark.db

import idb.Table

/**
  * Configuration of a benchmark db simulator
  *
  * @param name               Name of the db
  * @param initIterations     Number of iteration executions during initialization
  * @param initIteration      Iteration function during initialization, which performs exactly one change at the table
  *                           per call. It gets provided the number of the iteration call as second parameter
  * @param iterations         Number of iteration executions during measurement
  * @param iteration          Iteration function during measurement, which performs exactly one change at the table per
  *                           call. It gets provided the number of the iteration call as second parameter
  * @param initIterationSpeed If iterator db mode is disabled, this factor defines, how many initIterations shall be
  *                           executed per baseInitIteration
  * @param iterationSpeed     If iterator db mode is disabled, this factor defines, how many iterations shall be
  *                           executed per baseIteration
  * @tparam T
  */
case class BenchmarkDBConfig[T](
								   name: String,
								   initIterations: Int,
								   protected[db] val initIteration: (Table[T], Int) => Unit,
								   iterations: Int,
								   protected[db] val iteration: (Table[T], Int) => Unit,
								   initIterationSpeed: Int = 1,
								   iterationSpeed: Int = 1
							   )