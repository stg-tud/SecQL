package idb.algebra.remote

import idb.algebra.RelationalAlgebraIREssentialsPackage
import idb.algebra.remote.placement._
import idb.algebra.remote.taint.StandardQueryTaint

trait PlacementStrategy
	extends CSPPlacementTransformer4
	with StandardQueryTaint
	//with StandardRemoteOptimization
{
	val IR : RelationalAlgebraIREssentialsPackage
}
