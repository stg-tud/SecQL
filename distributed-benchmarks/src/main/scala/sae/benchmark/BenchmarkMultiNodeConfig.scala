package sae.benchmark

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

trait BenchmarkMultiNodeConfig extends MultiNodeConfig {
	// Load default benchmark akka config from application.conf and standard configuration
//	commonConfig(ConfigFactory.load())
	commonConfig(ConfigFactory.defaultApplication())

}
