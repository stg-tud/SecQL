package idb.remote

import akka.actor.Actor
import idb.Relation
import idb.remote.observer.RemoteObservableAdapter

/**
  * Represents an operator, which is deployed on another host. It acts as RemoteObservable and observes and forwards the
  * observed events of it's internal operator tree.
  *
  * @param relation
  * @tparam Domain
  */
class RemoteOperator[Domain](val relation: Relation[Domain])
	extends RemoteObservableAdapter[Domain](relation) with Actor {

	override def receive: Receive = super.receive orElse {
		case Initialize =>
			initialize(relation)
		case Reset =>
			relation.reset()
		case Print =>
			val out = System.out //TODO: How to choose the correct printstream here?
			out.println(s"Actor[${self.path.toStringWithoutAddress}]{")
			relation.printNested(out, relation)(" ")
			out.println(s"}")
	}

	protected def initialize(relation: Relation[_]): Unit = {
		relation match {
			case receiver: RemoteReceiver[_] => receiver.deploy(context.system)
			case _ =>
		}

		relation.children.foreach(initialize)
	}
}
