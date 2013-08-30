package sae
package collections


/**
 * A relation backed by a multi set for efficient access to elements.
 * Each element may have multiple occurrences in this relation.
 */
trait Bag[V]
    extends BagRelation[V]
    with Collection[V]
    with QueryResult[V]
{

    import com.google.common.collect.HashMultiset

    private val data: HashMultiset[V] = HashMultiset.create[V]()

    def foreach[U](f: V => U)
    {
        val it = data.iterator ()
        while (it.hasNext) {
            f (it.next ())
        }
    }

    def foreachWithCount[T](f: (V, Int) => T) {
        val it = data.entrySet ().iterator ()
        while (it.hasNext) {
            val e = it.next ()
            f (e.getElement, e.getCount)
        }
    }


    def contains(v: V) = data.contains (v)

    def isDefinedAt(v: V) = {
        data.contains (v)
    }

    def elementCountAt[T >: V](v: T) = {
        data.count (v)
    }

    def size: Int =
    {
        data.size ()
    }

    def singletonValue: Option[V] =
    {
        if (size != 1)
            None
        else
            Some (data.iterator ().next ())
    }

    def add_element(v: V)
    {
        data.add (v)
    }


    def add_element(v: V, count : Int)
    {
        data.add (v, count)
    }

    def remove_element(v: V)
    {
        data.remove (v)
    }

    def remove_element(v: V, count : Int)
    {
        data.remove (v, count)
    }

    @deprecated
    def update(oldV: V, newV: V) = update_element(oldV, newV)

    def update_element(oldV: V, newV: V)
    {
        val count = data.count (oldV)
        data.setCount (oldV, 0)
        data.add (newV, count)
        element_updated (oldV, newV)
    }

    def update_element(oldV: V, newV: V, count : Int)
    {
        val oldCount = data.count (oldV)

        assert(oldCount >= count)

        data.setCount (oldV, oldCount - count)
        data.add (newV, count)
        element_updated (oldV, newV)
    }
}