/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae

import capabilities.{LazyInitialized, Listable, Size, Iterable}

/**
 * An index in a database provides fast access to the values <code>V</code>
 * of a relation of Vs via keys of type <code>K</code>.</br>
 * The index assumes that the underlying relation can change.
 * Thus each index is required to register as an observer of the base relation.
 * Updates to the relation must be propagated to observers of the index.
 * </br>
 * Due to the contravariant nature of observers
 * (i.e., an Observer[Object] can still register as an observer for this index),
 * the values have to remain invariant. But the relation may still vary.
 *
 * Indices are to be updated prior to other relations and need not register themselves.
 * Re-using only the observers would have
 * yielded update order considerations where clients are forced to rely on the index as
 * underlying collection instead of the collection itself.
 *
 * Especially operators with a left and right operand, that rely on both being correctly indexed during update,
 * must NOT rely on the indices, but rather on the operands.
 *
 */
trait Index[K, V]
    extends Observable[(K, V)]
    with Iterable[(K, V)]
    with LazyInitialized
    with Size
    with Listable[(K,V)]
{

    def relation: Relation[V]

    def keyFunction: V => K

    // an index is lazy isInitialized by calling build
    def lazyInitialize() {
        relation.foreach (
            v => {
                put (keyFunction (v), v)
            }
        )
    }

    /**
     * Returns the size of the view in terms of elements.
     * This can be a costly operation.
     * Implementors should cache the value in a self-maintained view, but clients can not rely on this.
     */
    def size  : Int

    /**
     * TODO this is currently enabled to iterate uniquely over the keyset for bag indices.
     * The question remains whether this is needed, or if bag indices should always make
     * computations over number of contained elements since they basically are sets of the type
     * { (elem, count) } anyway.
     */
    def foreachKey[U](f: (K) => U)



    def put(key: K, value: V)

    def get(key: K): Option[Traversable[V]]

    def isDefinedAt(key: K): Boolean

    def elementCountAt(key: K): Int

    def getOrElse(key: K, f: => Traversable[V]): Traversable[V] = get (key).getOrElse (f)

    def add_element(key: K, value: V)

    def remove_element(key: K, value: V)

    def update_element(oldKey: K, oldV: V, newKey: K, newV: V)

    def updated(oldV: V, newV: V) {
        if (oldV == newV)
            return
        val k1 = keyFunction (oldV)
        val k2 = keyFunction (newV)
        update_element (k1, oldV, k2, newV)
        element_updated ((k1, oldV), (k2, newV))
    }

    def removed(v: V) {
        val k = keyFunction (v)
        remove_element (k, v)
        element_removed ((k, v))
    }

    def added(v: V) {
        val k = keyFunction (v)
        add_element (k, v)
        element_added ((k, v))
    }



}
