package idb.syntax.iql

import idb.query.QueryEnvironment
import idb.query.taint.Taint
import idb.syntax.iql.IR._

/**
  * An operator to overwrite the taint of a relation
  */
object RECLASS {

	/**
	  *
	  * @param relation The relation to alter
	  * @param newTaint The taint that shall be set instead of the current
	  * @param env      The QueryEnvironment
	  * @tparam Domain
	  * @return
	  */
	def apply[Domain : Manifest](
		relation : Rep[Query[Domain]],
		newTaint : Taint
	)(implicit env : QueryEnvironment) : Rep[Query[Domain]] =
		reclassification(relation, newTaint)
}
