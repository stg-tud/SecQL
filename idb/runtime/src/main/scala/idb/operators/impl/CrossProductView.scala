package idb.operators.impl

import idb.operators.CrossProduct
import idb.{Extent, Relation}
import idb.observer.{Observable, NotifyObservers, Observer}

/**
 * The cross product does not really require the underlying relations to be materialized directly.
 * But it requires some point of materialization.
 */
class CrossProductView[DomainA, DomainB, Range](val left: Relation[DomainA],
												val right: Relation[DomainB],
												val projection: (DomainA, DomainB) => Range,
												override val isSet: Boolean)
	extends CrossProduct[DomainA, DomainB, Range] with NotifyObservers[Range] {

	left addObserver LeftObserver
	right addObserver RightObserver

	override protected def lazyInitialize() {
		/* do nothing */
	}

	override protected def childObservers(o: Observable[_]): Seq[Observer[_]] = {
		if (o == left) {
			return List(LeftObserver)
		}
		if (o == right) {
			return List(RightObserver)
		}
		Nil
	}

	/**
	 * Applies f to all elements of the view.
	 */
	override def foreach[T](f: (Range) => T) {
		left.foreach(
			a => {
				right.foreach(
					b => {
						f(projection(a, b))
					}
				)
			}
		)
	}

	object LeftObserver extends Observer[DomainA] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on left relation
		override def updated(oldA: DomainA, newA: DomainA) {
			right.foreach(
				b => {
					notify_removed(projection(oldA, b))
					notify_added(projection(newA, b))
				}
			)
		}

		override def removed(v: DomainA) {
			right.foreach(
				b => {
					notify_removed(projection(v, b))
				}
			)

			if (left == right)
				notify_removed(projection(v,v.asInstanceOf[DomainB]))
		}

		override def added(v: DomainA) {
			right.foreach(
				b => {
					notify_added(projection(v, b))
				}
			)
		}
	}

	object RightObserver extends Observer[DomainB] {

		override def endTransaction() {
			notify_endTransaction()
		}

		// update operations on right relation
		override def updated(oldB: DomainB, newB: DomainB) {
			left.foreach(
				a => {
					notify_removed(projection(a, oldB))
					notify_added(projection(a, newB))
				}
			)
		}

		override def removed(v: DomainB) {
			left.foreach(
				a => {
					notify_removed(projection(a, v))
				}
			)

			if (left == right)
				notify_removed(projection(v.asInstanceOf[DomainA],v))
		}

		override def added(v: DomainB) {
			left.foreach(
				a => {
					notify_added(projection(a, v))
				}
			)
		}
	}


}

object CrossProductView {
	def apply[DomainA, DomainB](left: Relation[DomainA], right: Relation[DomainB], isSet: Boolean) = {

		//TODO Remove this. Better: implement 'foreach' in Extent
		var leftR = left
		var rightR = right

		if(left.isInstanceOf[Extent[DomainA]])
			leftR = left.asMaterialized

		if(right.isInstanceOf[Extent[DomainB]])
			rightR = right.asMaterialized


		new CrossProductView[DomainA, DomainB, (DomainA, DomainB)](leftR,rightR,(l : DomainA, r : DomainB) => (l,r),isSet)
	}
}