package idb.remote

trait ControlMessage extends Serializable

case object Initialize extends ControlMessage
case object Initialized extends ControlMessage
case object SetupStream extends ControlMessage
case object Reset extends ControlMessage
case object ResetCompleted extends ControlMessage
case class Print(prefix: String) extends ControlMessage
case object PrintCompleted extends ControlMessage