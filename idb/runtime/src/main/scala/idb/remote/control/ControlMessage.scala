package idb.remote.control

import akka.actor.ActorRef

trait ControlMessage extends Serializable

case class SendTo(ref: ActorRef) extends ControlMessage
case object Initialize extends ControlMessage
case object Reset extends ControlMessage
case object Print extends ControlMessage
