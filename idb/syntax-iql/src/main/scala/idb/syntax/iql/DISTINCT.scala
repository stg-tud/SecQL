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
package idb.syntax.iql

import idb.syntax.iql.IR._
import idb.syntax.iql.impl._

/**
 *
 * @author Ralf Mitschke
 *
 */

object DISTINCT
{

    def apply[Domain: Manifest, Range: Manifest] (
        projection: Rep[Domain] => Rep[Range]
    ): DISTINCT_FUNCTION_1[Domain, Range] =
        DistinctFunction1 (projection)


    def apply[DomainA: Manifest, DomainB: Manifest, Range: Manifest] (
        projection: (Rep[DomainA], Rep[DomainB]) => Rep[Range]
    ): DISTINCT_FUNCTION_2[DomainA, DomainB, Range] =
        DistinctFunction2 (projection)


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Range: Manifest] (
        projection: (Rep[DomainA], Rep[DomainB], Rep[DomainC]) => Rep[Range]
    ): DISTINCT_FUNCTION_3[DomainA, DomainB, DomainC, Range] =
        DistinctFunction3 (projection)


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        projection: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD]) => Rep[Range]
    ): DISTINCT_FUNCTION_4[DomainA, DomainB, DomainC, DomainD, Range] =
        DistinctFunction4 (projection)


    def apply[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        projection: (Rep[DomainA], Rep[DomainB], Rep[DomainC], Rep[DomainD], Rep[DomainE]) => Rep[Range]
    ): DISTINCT_FUNCTION_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range] =
        DistinctFunction5 (projection)


    def apply (x: STAR_KEYWORD): DISTINCT_FUNCTION_STAR =
        DistinctFunctionStar

}
