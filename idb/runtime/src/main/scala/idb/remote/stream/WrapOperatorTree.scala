package idb.remote.stream

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, Inlet, Outlet, UniformFanInShape}
import idb.Relation
import idb.observer.Observer
import idb.remote._

import scala.collection.immutable

/**
  * Combine Strategy for Akka streams to wrap an entire i3QL operator tree
  *
  * @param leaves
  * @param root
  * @param inputPorts
  * @tparam T
  */
case class WrapOperatorTree[T](leaves: Seq[Observer[Any]], root: Relation[T])(inputPorts: Int) extends GraphStage[UniformFanInShape[DataMessage[Any], DataMessage[T]]] {

	val inputs: immutable.IndexedSeq[Inlet[DataMessage[_]]] =
		Vector.tabulate(inputPorts)(i ⇒ Inlet("RemoteOperatorTree.in" + i))
	val output: Outlet[DataMessage[T]] = Outlet("RemoteOperatorTree.out")

	override val shape = UniformFanInShape(output, inputs: _*)

	override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with OutHandler {

		private val pendingInputs = Array.fill(inputs.size)(false)

		private def inputsPending: Boolean = pendingInputs.nonEmpty

		private var runningUpstreams = inputPorts

		private def upstreamsClosed = runningUpstreams == 0

		private val bufferedOutput = StreamAdapter.bufferFromObservable(root, tryPush)

		private def outputPending: Boolean = bufferedOutput.nonEmpty

		private def tryPush(): Unit = {
			while (outputPending && isAvailable(output))
				push(output, bufferedOutput.dequeue())
		}

		private def tryCompleteStage(): Unit = {
			if (upstreamsClosed && !inputsPending && !outputPending)
				completeStage()
		}


		private def grabInput(input: Inlet[DataMessage[_]]): Unit = {
			StreamAdapter.toObserver(grab(input), leaves(inputs.indexOf(input)))
			tryPull(input)
		}

		/**
		  * Initially, signal to all inputs that messages can be pushed
		  */
		override def preStart(): Unit =
			inputs.indices foreach {
				inputId => tryPull(inputs(inputId))
			}

		inputs foreach (input => {
			/**
			  * Register for each input channel a handler
			  */
			setHandler(input, new InHandler {

				override def onPush(): Unit = {
					if (!outputPending)
						grabInput(input)
					else
						pendingInputs(inputs.indexOf(input)) = true
				}

				override def onUpstreamFinish(): Unit = {
					runningUpstreams -= 1
					tryCompleteStage()
				}

			})
		})

		override def onPull(): Unit = {
			if (outputPending) {
				tryPush()
			}
			else if (inputsPending) {
				inputs foreach { input =>
					if (pendingInputs(inputs.indexOf(input))) {
						pendingInputs(inputs.indexOf(input)) = false
						grabInput(input)
					}
				}
			}
			tryCompleteStage()
		}

		setHandler(output, this)
	}
}
