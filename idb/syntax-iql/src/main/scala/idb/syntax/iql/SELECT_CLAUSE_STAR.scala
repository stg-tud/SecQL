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

/**
 *
 * @author Ralf Mitschke
 */
trait SELECT_CLAUSE_STAR
{

    def FROM[Domain: Manifest] (
        relation: Rep[Query[Domain]]
    ): FROM_CLAUSE_1[Domain, Domain, Domain]
	//	with CAN_GROUP_CLAUSE_1[Domain, Domain, Domain]


    def FROM[DomainA: Manifest, DomainB: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]]
    ): FROM_CLAUSE_2[(DomainA, DomainB), DomainA, DomainB, (DomainA, DomainB)]
	//	with CAN_GROUP_CLAUSE_2[(DomainA, DomainB), DomainA, DomainB, (DomainA, DomainB)]


    def FROM[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]]
    ): FROM_CLAUSE_3[(DomainA, DomainB, DomainC), DomainA, DomainB, DomainC, (DomainA, DomainB, DomainC)]
	//	with CAN_GROUP_CLAUSE_3[(DomainA, DomainB, DomainC), DomainA, DomainB, DomainC, (DomainA, DomainB, DomainC)]


    def FROM[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]]
    ): FROM_CLAUSE_4[(DomainA, DomainB, DomainC, DomainD), DomainA, DomainB, DomainC, DomainD, (DomainA, DomainB, DomainC, DomainD)]
	//	with CAN_GROUP_CLAUSE_4[(DomainA, DomainB, DomainC, DomainD), DomainA, DomainB, DomainC, DomainD, (DomainA, DomainB, DomainC, DomainD)]


	def FROM[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest] (
        relationA: Rep[Query[DomainA]],
        relationB: Rep[Query[DomainB]],
        relationC: Rep[Query[DomainC]],
        relationD: Rep[Query[DomainD]],
        relationE: Rep[Query[DomainE]]
    ): FROM_CLAUSE_5[(DomainA, DomainB, DomainC, DomainD, DomainE), DomainA, DomainB, DomainC, DomainD, DomainE, (DomainA, DomainB, DomainC, DomainD, DomainE)]
	//	with CAN_GROUP_CLAUSE_5[(DomainA, DomainB, DomainC, DomainD, DomainE), DomainA, DomainB, DomainC, DomainD, DomainE, (DomainA, DomainB, DomainC, DomainD, DomainE)]

}