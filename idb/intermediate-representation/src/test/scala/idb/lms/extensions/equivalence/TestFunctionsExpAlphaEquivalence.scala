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

import scala.virtualization.lms.common.{EqualExp, LiftAll}
import org.junit.Test

/**
 *
 * @author Ralf Mitschke
 */
class TestFunctionsExpAlphaEquivalence
    extends LiftAll with EqualExp with FunctionsExpAlphaEquivalence with AssertAlphaEquivalence
{

    @Test
    def testBoundVarsEq () {
        val f1 = fun ((x: Rep[Int]) => x)
        val f2 = fun ((y: Rep[Int]) => y)

        assertEquivalent (f1, f2)
    }

    @Test
    def testFreeVarsNotEq () {
        val u = fresh[Int]
        val v = fresh[Int]
        val f1 = fun ((x: Rep[Int]) => u)
        val f2 = fun ((y: Rep[Int]) => v)

        assertNotEquivalent (f1, f2)
    }


    @Test
    def testConstEq () {
        val f1 = fun ((x: Rep[Int]) => unit (1))
        val f2 = fun ((y: Rep[Int]) => unit (1))

        assertEquivalent (f1, f2)
    }

    @Test
    def testConstNotEq () {
        val f1 = fun ((x: Rep[Int]) => unit (1))
        val f2 = fun ((y: Rep[Int]) => unit (5))

        assertNotEquivalent (f1, f2)
    }

    @Test
    def testApplicationEq () {
        val f1 = fun ((x: Rep[Int]) => x)
        val f2 = fun ((y: Rep[Int]) => y)

        val f3 = fun ((u: Rep[Int]) => f1 (f2 (u)))
        val f4 = fun ((v: Rep[Int]) => f2 (f1 (v)))

        assertEquivalent (f3, f4)
    }

    @Test
    def testApplicationNotEq () {
        val f1 = fun ((x: Rep[Int]) => x)
        val f2 = fun ((y: Rep[Int]) => y)

        val f3 = fun ((u: Rep[Int]) => f1 (f2 (u)))
        val f4 = fun ((v: Rep[Int]) => f2 (f1 (unit(1))))

        assertNotEquivalent (f3, f4)
    }
}
