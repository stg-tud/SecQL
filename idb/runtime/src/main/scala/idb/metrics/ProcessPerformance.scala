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
	  * @param triggerGc If true, system is asked to run the garbage collection. This might delay the execution.
	  *                  Moreover, it is not guaranteed that the JVM indeed performs a garbage collection
	  * @return Memory used by the program in bytes
	  */
	def memory(triggerGc: Boolean = false): Long = {
		if (triggerGc)
			System.gc()

		runtime.totalMemory() - runtime.freeMemory()
	}

}