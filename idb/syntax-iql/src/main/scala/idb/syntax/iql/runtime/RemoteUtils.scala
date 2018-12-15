package idb.syntax.iql.runtime

import akka.actor.{ActorPath, ActorRef, ActorSystem, Deploy, Props}
import akka.pattern.ask
import akka.remote.RemoteScope
import akka.util.Timeout
import idb.Relation
import idb.remote.{Initialized, RemoteConnector}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object RemoteUtils {

	implicit val timeout: Timeout = Timeout(10 seconds)

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

		Await.result(ref ? Initialized, timeout.duration)
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

	def receiver[Domain](controllerPath: ActorPath): RemoteConnector[Domain] = {
		RemoteConnector[Domain](controllerPath)
	}

	def deployReceiver[Domain](system: ActorSystem, operatorPath: ActorPath): RemoteConnector[Domain] = {
		val r = receiver[Domain](operatorPath)
		r.initialize(system)
		r
	}

	def receiver[Domain](controllerRef: ActorRef): RemoteConnector[Domain] = {
		RemoteConnector[Domain](controllerRef)
	}

	def deployReceiver[Domain](system: ActorSystem, operatorRef: ActorRef): RemoteConnector[Domain] = {
		val r = receiver[Domain](operatorRef)
		r.initialize(system)
		r
	}
}
