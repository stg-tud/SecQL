package idb.algebra.remote

import idb.algebra.RelationalAlgebraIREssentialsPackage
import idb.algebra.remote.placement.CSPPlacementTransformer2
import idb.algebra.remote.taint.StandardQueryTaint

trait PlacementStrategy
	extends CSPPlacementTransformer2
	with StandardQueryTaint
	//with StandardRemoteOptimization
{
	val IR : RelationalAlgebraIREssentialsPackage
}
