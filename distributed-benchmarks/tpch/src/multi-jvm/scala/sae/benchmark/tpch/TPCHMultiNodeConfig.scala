package sae.benchmark.tpch

import akka.remote.testconductor.RoleName
import sae.benchmark.BenchmarkMultiNodeConfig

object TPCHMultiNodeConfig extends BenchmarkMultiNodeConfig {
	//Data nodes
	val node_data_customer: RoleName = role("node_data_customer")
	val node_data_nation: RoleName = role("node_data_nation")
	val node_data_orders: RoleName = role("node_data_orders")
	val node_data_part: RoleName = role("node_data_part")
	val node_data_partsupp: RoleName = role("node_data_partsupp")
	val node_data_region: RoleName = role("node_data_region")
	val node_data_supplier: RoleName = role("node_data_supplier")
	//processing nodes
	val node_process_finance: RoleName = role("node_process_finance")
	val node_process_purchasing: RoleName = role("node_process_purchasing")
	val node_process_shipping: RoleName = role("node_process_shipping")
	val node_process_geographical: RoleName = role("node_process_geographical")
	val node_process_private: RoleName = role("node_process_private")
	//client node
	val node_client: RoleName = role("node_client")
}
