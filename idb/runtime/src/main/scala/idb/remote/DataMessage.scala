package idb.remote

/**
 * Sealed trait to simplify pickling.
 */
sealed trait DataMessage[V] extends Serializable
case class Added[V](d: V) extends DataMessage[V]
case class Removed[V](d: V) extends DataMessage[V]
case class Updated[V](oldV: V, newV: V) extends DataMessage[V]
case class AddedAll[V](d: Seq[V]) extends DataMessage[V]
case class RemovedAll[V](d: Seq[V]) extends DataMessage[V]
