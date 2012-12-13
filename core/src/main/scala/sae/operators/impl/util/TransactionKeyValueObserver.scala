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
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
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
package sae.operators.impl.util

import sae.deltas.{Deletion, Addition, Update}
import com.google.common.collect.HashMultiset
import sae.Observer

/**
 *
 * @author Ralf Mitschke
 *
 */

trait TransactionKeyValueObserver[Key, Domain]
    extends Observer[Domain]
{

    def keyFunc : Domain => Key

    var additions = com.google.common.collect.ArrayListMultimap.create[Key, Domain]()

    var deletions = com.google.common.collect.ArrayListMultimap.create[Key, Domain]()

    def clear() {
        additions = com.google.common.collect.ArrayListMultimap.create[Key, Domain]()
        deletions = com.google.common.collect.ArrayListMultimap.create[Key, Domain]()
    }

    // update operations on right relation
    def updated(oldV: Domain, newV: Domain) {
        additions.put (keyFunc (newV), newV)
        deletions.put (keyFunc (oldV), oldV)
    }

    def removed(v: Domain) {
        deletions.put (keyFunc (v), v)
    }

    def added(v: Domain) {
        additions.put (keyFunc (v), v)
    }

    def updated[U <: Domain](update: Update[U]) {
        throw new UnsupportedOperationException
    }

    def modified[U <: Domain](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
        throw new UnsupportedOperationException
    }

}