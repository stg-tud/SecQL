package idb.remote

import akka.actor.{Actor, ActorLogging}
import akka.stream.scaladsl.{Source, StreamRefs}
import akka.stream.{ActorMaterializer, SourceRef}
import akka.util.Timeout
import idb.Relation
import idb.remote.stream.{RemotePublisher, RemotePublisherObserver, StreamAdapter, WrapOperatorTree}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}


/**
  * Represents an operator, which is deployed on another host. It acts as RemoteObservable and observes and forwards the
  * observed events of it's internal operator tree.
  *
  * @param relation
  * @tparam Domain
  */
class RemoteOperator[Domain](val relation: Relation[Domain])
	extends Actor with ActorLogging {

	implicit val timeout: Timeout = Timeout(10.seconds)
	implicit val mat: ActorMaterializer = ActorMaterializer()(context)

	override def receive: Receive = {
		case Initialized =>
			initializeOperator(relation)
			sender ! Initialize
		case SetupStream =>
			sender ! Await.result(initializeStream(), timeout.duration)
		case Reset =>
			relation.reset()
			sender ! ResetCompleted
		case Print =>
			val out = System.out //TODO: How to choose the correct printstream here?
			out.println(s"Actor[${self.path.toStringWithoutAddress}]{")
			relation.printNested(out, relation)(" ")
			out.println(s"}")
			sender ! PrintCompleted
	}

	private val remoteConnections = ArrayBuffer[RemoteConnector[Any]]()

	protected def initializeOperator(relation: Relation[_]): Unit = {
		relation match {
			case connection: RemoteConnector[Any] =>
				connection.initialize(context.system)
				remoteConnections += connection
			case _ =>
		}

		relation.children.foreach(initializeOperator)
	}

	protected def rootRelation: Relation[Domain] = relation

	protected def initializeStream(): Future[SourceRef[DataMessage[Domain]]] = {
		var stream: Source[DataMessage[Domain], _] = null

		if (remoteConnections.isEmpty) {
			// This operator is a leaf in the tree and therefore a data source itself
			log.info(s"Initializing leaf remote operator of $relation")
			stream = relation match {
				// If already a publisher, pull based back pressure can be ensured without loss
				case view: RemotePublisher[Domain] =>
					Source.fromPublisher(view)
				// Otherwise buffering is necessary
				case _ =>
					Source.fromPublisher(RemotePublisherObserver[Domain](rootRelation))
			}
		}
		else if (remoteConnections.size == 1) {
			val remoteConnection = remoteConnections(0)
			val remoteSource = remoteConnection.source
			log.info(s"Initializing unary remote operator of $relation")
			stream = remoteSource
				.mapConcat(StreamAdapter.wrapLinearOperatorTree(remoteConnection, rootRelation))
		}
		else {
			log.info(s"Initializing remote operator tree of $relation")
			val firstSource = remoteConnections(0).source
			val secondSource = remoteConnections(1).source
			val otherSources = remoteConnections.slice(2, remoteConnections.size).map(_.source)
			stream = Source.combine(firstSource, secondSource, otherSources: _*)(WrapOperatorTree(remoteConnections, rootRelation))
		}

		stream.runWith(StreamRefs.sourceRef())
	}

}