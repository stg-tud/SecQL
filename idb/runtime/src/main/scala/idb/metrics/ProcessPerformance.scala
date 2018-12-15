package idb.metrics

import java.lang.management.ManagementFactory

import com.sun.management.OperatingSystemMXBean

object ProcessPerformance {

	lazy val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean.asInstanceOf[com.sun.management.OperatingSystemMXBean]
	lazy val runtime: Runtime = Runtime.getRuntime

	/**
	  * @return "Recent CPU usage" share of the entire process in relation to the entire systems CPU power in a range
	  *         [0.0, 1.0]
	  */
	def cpuLoad(): Double = {
		osBean.getProcessCpuLoad
	}

	/**
	  * @return CPU time used of the process in nano seconds
	  */
	def cpuTime(): Long = {
		osBean.getProcessCpuTime
	}

	/**
	  * Rough but quick approach to get the used memory of the program. Includes additional used memory that has not
	  * been clean up already by the garbage collector. Fast but inaccurate.
	  * Based on https://cruftex.net/2017/03/28/The-6-Memory-Metrics-You-Should-Track-in-Your-Java-Benchmarks.html#metric-used-memory-after-forced-gc-and-settling
	  *
	  * @return Memory used by the program in bytes
	  */
	def memory(): Long =
		ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getUsed +
			ManagementFactory.getMemoryMXBean.getNonHeapMemoryUsage.getUsed

	/**
	  * Evaluates the number of executed garbage collection cycles.
	  * Based on https://cruftex.net/2017/03/28/The-6-Memory-Metrics-You-Should-Track-in-Your-Java-Benchmarks.html#metric-used-memory-after-forced-gc-and-settling
	  *
	  * @return Total number of GCs
	  */
	def gcCount(): Long = {
		var totalCount = 0L

		import scala.collection.JavaConverters._
		ManagementFactory.getGarbageCollectorMXBeans.asScala.foreach { gcBean =>
			val count = gcBean.getCollectionCount()
			if (count != -1)
				totalCount += count
		}

		totalCount
	}

	/**
	  * Triggers a garbage collection and blocks the thread until it completed to deliver the used memory directly
	  * after GC. This is a blocking operation.
	  * Based on https://cruftex.net/2017/03/28/The-6-Memory-Metrics-You-Should-Track-in-Your-Java-Benchmarks.html#metric-used-memory-after-forced-gc-and-settling
	  *
	  * @return Memory usage after GC in bytes
	  */
	def memoryAfterGC(): Long = {
		val gcBefore = gcCount()
		System.gc()
		while (gcCount() == gcBefore) Unit
		memory()
	}

	/**
	  * Runs garbage collections and wait for their result, until they do not decrease anymore. This is a costly
	  * operation, but delivers accurate results with low variance.
	  *
	  * @return Memory usage after multiple GC and with settled memory usage in bytes
	  */
	def memoryAfterSettling(): Long = {
		var settledMemory = Long.MaxValue
		var measurement = memoryAfterGC()

		do {
			settledMemory = measurement
			measurement = memoryAfterGC()
		} while (measurement < settledMemory)

		settledMemory
	}

}