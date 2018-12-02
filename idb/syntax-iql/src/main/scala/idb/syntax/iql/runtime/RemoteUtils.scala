package idb.syntax.iql.runtime

import akka.actor.{ActorPath, ActorRef, ActorSystem, Deploy, Props}
import akka.remote.RemoteScope
import idb.Relation
import idb.remote.{Initialize, RemoteReceiver}

object RemoteUtils {
	/**
	  * Deploys a operator relation on the given node.
	  *
	  * @return A reference to an access actor that controls the given relation.
	  */
	def deployOperator[Domain](system: ActorSystem, node: ActorPath)(relation: Relation[Domain]): ActorRef = {

		val ref = system.actorOf(
			Props(classOf[CompilingRemoteOperator[Domain]], relation)
				.withDeploy(Deploy(scope = RemoteScope(node.address)))
		)

		ref ! Initialize
		ref
	}

	/**
	  * Creates an remote actor for a relation, so that it can be referenced by other remotes.
	  *
	  * @return A reference to an access actor that controls the given relation.
	  */
	def createOperator(system: ActorSystem)(id: String, relation: Relation[_]): ActorRef = {
		system.actorOf(Props(classOf[CompilingRemoteOperator[_]], relation), id)
	}

	def receiver[Domain](controllerPath: ActorPath): RemoteReceiver[Domain] = {
		RemoteReceiver[Domain](controllerPath)
	}

	def deployReceiver[Domain](system: ActorSystem, operatorPath: ActorPath): RemoteReceiver[Domain] = {
		val r = receiver[Domain](operatorPath)
		r.deploy(system)
		r
	}

	def receiver[Domain](controllerRef: ActorRef): RemoteReceiver[Domain] = {
		RemoteReceiver[Domain](controllerRef)
	}

	def deployReceiver[Domain](system: ActorSystem, operatorRef: ActorRef): RemoteReceiver[Domain] = {
		val r = receiver[Domain](operatorRef)
		r.deploy(system)
		r
	}
}
