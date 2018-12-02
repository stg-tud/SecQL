package idb.syntax.iql.runtime

import idb.Relation
import idb.remote.control.RemoteController

/**
  * This is a RemoteController, which is able to unbox and compile query relations in initialization
  *
  * @param relation
  * @tparam Domain
  */
class CompilingRemoteController[Domain](relation: Relation[Domain])
	extends RemoteController[Domain](relation) {

	override protected def initialize(relation: Relation[_]): Unit = {
		CompilerBinding.initialize(relation, false)

		super.initialize(relation)
	}
}
