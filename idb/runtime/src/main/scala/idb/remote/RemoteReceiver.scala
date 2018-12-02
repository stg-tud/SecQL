package idb.remote

import java.io.PrintStream

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}
import idb.Relation
import idb.observer.{NotifyObservers, Observable, Observer}
import idb.remote.observer.{RemoteObservableProxy, RemoteObserver, RemoteObserverProxy}

import scala.concurrent.Await

object RemoteReceiver {
	def apply[Domain](remoteOperatorRef: ActorRef): RemoteReceiver[Domain] =
		RemoteReceiver[Domain](remoteOperatorRef, null)

	def apply[Domain](remoteOperatorPath: ActorPath): RemoteReceiver[Domain] =
		RemoteReceiver[Domain](null, remoteOperatorPath)
}

/**
  * Receiving counterpart of RemoteOperator, which acts as RemoteObserver and normal Observable
  *
  * @param remoteOperatorRef
  * @param remoteOperatorPath
  * @tparam Domain
  */
case class RemoteReceiver[Domain](remoteOperatorRef: ActorRef, remoteOperatorPath: ActorPath)
	extends Relation[Domain] with NotifyObservers[Domain] {

	private var remoteOperatorActor: ActorRef = _

	/**
	  * Setups connection to children actor
	  */
	def deploy(system: ActorSystem): Unit = {
		if (remoteOperatorRef == null) {
			import scala.concurrent.duration._
			val timeout = 10.seconds

			// Retrieve the actor reference of the RemoteOperator
			val actorSelection = system.actorSelection(remoteOperatorPath).resolveOne(timeout)
			val remoteOperatorRefByPath = Await.result(actorSelection, timeout)
			internalDeploy(system, remoteOperatorRefByPath)
		}
		else
			internalDeploy(system, remoteOperatorRef)
	}

	protected def internalDeploy(system: ActorSystem, remoteOperatorRef: ActorRef): Unit = {
		if (remoteOperatorActor != null)
			throw new RuntimeException("RemoteOperator is already deployed")

		remoteOperatorActor = remoteOperatorRef

		val remoteOperator = RemoteObservableProxy(remoteOperatorRef)
		val observerActor = system.actorOf(Props(classOf[Observer], this))
		val localObserver = RemoteObserverProxy(observerActor)

		remoteOperator.addObserver(localObserver)
	}

	override protected[idb] def resetInternal(): Unit =
		remoteOperatorActor ! Reset

	override protected[idb] def printInternal(out: PrintStream)(implicit prefix: String = " "): Unit =
		remoteOperatorActor ! Print

	override def isSet: Boolean = false

	override def foreach[T](f: Domain => T): Unit = {}

	override def children: Seq[Relation[_]] = Nil

	class Observer extends RemoteObserver[Domain] {
		override def added(v: Domain): Unit = notify_added(v)

		override def addedAll(vs: Seq[Domain]): Unit = notify_addedAll(vs)

		override def updated(oldV: Domain, newV: Domain): Unit = notify_updated(oldV, newV)

		override def removed(v: Domain): Unit = notify_removed(v)

		override def removedAll(vs: Seq[Domain]): Unit = notify_removedAll(vs)
	}

}