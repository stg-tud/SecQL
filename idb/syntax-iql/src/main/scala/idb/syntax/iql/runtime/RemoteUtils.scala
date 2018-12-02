package idb.syntax.iql.runtime

import akka.actor.{ActorPath, ActorRef, ActorSystem, Deploy, Props}
import akka.remote.RemoteScope
import idb.Relation
import idb.remote.Initialize
import idb.remote.relation.{PathRemoteReceiver, RefRemoteReceiver, RemoteReceiver}

object RemoteUtils {
	/**
	  * Deploys a relation on the given node.
	  *
	  * @return A reference to an access actor that controls the given relation.
	  */
	def deployController[Domain](system: ActorSystem, node: ActorPath)(relation: Relation[Domain]): ActorRef = {

		val ref = system.actorOf(
			Props(classOf[CompilingRemoteController[Domain]], relation)
				.withDeploy(Deploy(scope = RemoteScope(node.address)))
		)

		ref ! Initialize
		ref
	}

	/**
	  * Creates an controlling actor for a relation, so that it can be referenced by other remotes.
	  *
	  * @return A reference to an access actor that controls the given relation.
	  */
	def createController(system: ActorSystem)(id: String, relation: Relation[_]): ActorRef = {
		system.actorOf(Props(classOf[CompilingRemoteController[_]], relation), id)
	}

	def relation[Domain](controllerPath: ActorPath): RemoteReceiver[Domain] = {
		PathRemoteReceiver[Domain](controllerPath)
	}

	def deployQuery[Domain](system: ActorSystem, controllerPath: ActorPath): RemoteReceiver[Domain] = {
		val r = relation[Domain](controllerPath)
		r.deploy(system)
		r
	}

	def relation[Domain](controllerRef: ActorRef): RemoteReceiver[Domain] = {
		RefRemoteReceiver[Domain](controllerRef)
	}

	def deployQuery[Domain](system: ActorSystem, controllerRef: ActorRef): RemoteReceiver[Domain] = {
		val r = relation[Domain](controllerRef)
		r.deploy(system)
		r
	}
}
