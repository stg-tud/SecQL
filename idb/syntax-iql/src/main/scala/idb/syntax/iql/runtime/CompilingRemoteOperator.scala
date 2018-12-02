package idb.syntax.iql.runtime

import idb.Relation
import idb.remote.RemoteOperator

/**
  * This is a RemoteController, which is able to unbox and compile query relations in initialization
  *
  * @param relation
  * @tparam Domain
  */
class CompilingRemoteOperator[Domain](relation: Relation[Domain])
	extends RemoteOperator[Domain](relation) {

	override protected def initialize(relation: Relation[_]): Unit = {
		CompilerBinding.initialize(relation, false)

		super.initialize(relation)
	}
}
