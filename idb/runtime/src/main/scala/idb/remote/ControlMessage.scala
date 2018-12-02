package idb.remote

trait ControlMessage extends Serializable

case object Initialize extends ControlMessage
case object Reset extends ControlMessage
case object Print extends ControlMessage