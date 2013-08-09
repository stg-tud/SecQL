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
package idb.lms.extensions.equivalence

import scala.virtualization.lms.common.BaseExp
import scala.collection.mutable

/**
 *
 * @author Ralf Mitschke
 *
 */

trait BaseExpAlphaEquivalence
    extends BaseExp
    with BaseAlphaEquivalence
{

    type VarExp[+T] = Sym[T]

    def isEquivalent[A, B] (a: Exp[A], b: Exp[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (Const (x), Const (y)) => x == y
            //case (Variable (x), Variable (y)) => isEquivalent (x, y) // TODO is this type of expression even created?

            case (Def (x), Def (y)) =>
                throw new IllegalArgumentException (
                    "Expression types are unknown to alpha equivalence: " + x + " =?= " + y)

            case (x: Sym[_], y: Sym[_]) => x == y || renamings.canRename (x, y)

            case (x: Sym[_], y: Const[_]) => false

            case (x: Const[_], y: Sym[_]) => false

            case _ => throw new
                    IllegalArgumentException ("Expression types are unknown to alpha equivalence: " + a + " =?= " + b)
        }

    class MultiMapVariableRenamings ()
        extends VariableRenamings
    {

        private val map: mutable.MultiMap[VarExp[Any], VarExp[Any]] =
            new mutable.HashMap[VarExp[Any], mutable.Set[VarExp[Any]]] with mutable.MultiMap[VarExp[Any], VarExp[Any]]

        def add[T] (x: VarExp[T], y: VarExp[T]): VariableRenamings = {
            map.addBinding (x, y)
            map.addBinding (y, x)
            this
        }

        def canRename[T] (x: VarExp[T], y: VarExp[T]): Boolean = {
            map.entryExists (x, _ == y)
        }

    }

    def emptyRenaming = new MultiMapVariableRenamings ()

}