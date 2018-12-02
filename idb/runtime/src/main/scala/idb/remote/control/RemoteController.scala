package idb.remote.control

import akka.actor.{Actor, ActorRef}
import idb.Relation
import idb.observer.Observer
import idb.remote._
import idb.remote.receive.RemoteReceiver

import scala.collection.mutable

/**
  * Manages the lifecycle of a remote operator on a remote host
  *
  * @param relation
  * @tparam Domain
  */
class RemoteController[Domain](
								  val relation: Relation[Domain]
							  ) extends Actor with Observer[Domain] {
	relation.addObserver(this)

	private val registeredActors: mutable.Buffer[ActorRef] = mutable.Buffer.empty

	override def receive: Receive = {
		case SendTo(ref) =>
			registeredActors += ref
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

	override def updated(oldV: Domain, newV: Domain): Unit = {
		registeredActors foreach (a => a ! Updated(oldV, newV))
	}

	override def removed(v: Domain): Unit =
		registeredActors foreach (a => a ! Removed(v))

	override def removedAll(vs: Seq[Domain]): Unit =
		registeredActors foreach (a => a ! RemovedAll(vs))

	override def added(v: Domain): Unit = {

		registeredActors foreach (a => {
			a ! Added(v)
		})
	}


	override def addedAll(vs: Seq[Domain]): Unit = {
		registeredActors foreach (a => {
			a ! AddedAll(vs)
		})
	}

}
