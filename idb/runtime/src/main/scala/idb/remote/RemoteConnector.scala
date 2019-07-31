package idb.remote

import java.io.PrintStream

import akka.actor.{ActorPath, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.SourceRef
import akka.stream.scaladsl.Source
import akka.util.Timeout
import idb.Relation
import idb.observer.{NotifyObservers, Observer}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object RemoteConnector {
	def apply[Domain](remoteOperatorRef: ActorRef): RemoteConnector[Domain] =
		RemoteConnector[Domain](remoteOperatorRef, null)

	def apply[Domain](remoteOperatorPath: ActorPath): RemoteConnector[Domain] =
		RemoteConnector[Domain](null, remoteOperatorPath)
}

/**
  * Receiving counterpart of RemoteOperator, which acts as RemoteObserver and normal Observable
  *
  * @param remoteOperatorRef
  * @param remoteOperatorPath
  * @tparam Domain
  */
case class RemoteConnector[Domain](remoteOperatorRef: ActorRef, remoteOperatorPath: ActorPath)
	extends Relation[Domain] with Observer[Domain] with NotifyObservers[Domain] {

	private var remoteSource: Source[DataMessage[Domain], _] = _

	def source: Source[DataMessage[Domain], _] = {
		if (remoteSource == null)
			remoteSource = requestRemoteSource()
		remoteSource
	}

	private var remoteOperatorActor: ActorRef = _

	implicit val timeout: Timeout = Timeout(10.seconds)

	/**
	  * Setups connection to children actor
	  */
	def initialize(system: ActorSystem): Unit = {
		if (remoteOperatorActor != null)
			return

		remoteOperatorActor =
			if (remoteOperatorRef != null)
				remoteOperatorRef
			else {
				import scala.concurrent.duration._
				val timeout = 10.seconds

				// Retrieve the actor reference of the RemoteOperator
				val actorSelection = system.actorSelection(remoteOperatorPath).resolveOne(timeout)
				Await.result(actorSelection, timeout)
			}
	}

	private def requestRemoteSource(): Source[DataMessage[Domain], _] = {
		val outputStreamResponse = (remoteOperatorActor ? SetupStream)
			.mapTo[SourceRef[DataMessage[Domain]]]
		Await.result(outputStreamResponse, timeout.duration)
	}

	override protected[idb] def resetInternal(): Unit =
		Await.result(remoteOperatorActor ? Reset, timeout.duration)

	override protected[idb] def printInternal(out: PrintStream)(implicit prefix: String = ""): Unit =
		Await.result(remoteOperatorActor ? Print(prefix), timeout.duration)

	override def isSet: Boolean = false

	override def foreach[T](f: Domain => T): Unit = {}

	override def children: Seq[Relation[_]] = Nil

	override def added(v: Domain): Unit = notify_added(v)

	override def addedAll(vs: Seq[Domain]): Unit = notify_addedAll(vs)

	override def updated(oldV: Domain, newV: Domain): Unit = notify_updated(oldV, newV)

	override def removed(v: Domain): Unit = notify_removed(v)

	override def removedAll(vs: Seq[Domain]): Unit = notify_removedAll(vs)

}