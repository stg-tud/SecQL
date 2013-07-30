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

import UniversityDatabase._
import idb.schema.university._
import idb.syntax.iql.IR._
import org.junit.Assert._
import org.junit.{Ignore, Test}

/**
 *
 * @author Ralf Mitschke
 */
class TestBasicClauses2
{

    @Test
    def testCrossProduct2 () {
        val query = plan (
            SELECT (*) FROM(students, courses)
        )

        assertEquals (
            crossProduct (extent (students), extent (courses)),
            query
        )
    }

    @Test
    def testCrossProduct2Selection1st () {
        val query = plan (
            SELECT (*) FROM(students, courses) WHERE ((s: Rep[Student], c: Rep[Course]) => {
                s.firstName == "Sally"
            })
        )

        assertEquals (
            crossProduct (
                selection (extent (students), (s: Rep[Student]) => s.firstName == "Sally"),
                extent (courses)
            ),
            query
        )
    }

    @Test
    def testCrossProduct2Selection2nd () {
        val query = plan (
            SELECT (*) FROM(students, courses) WHERE ((s: Rep[Student], c: Rep[Course]) => {
                c.title.startsWith ("Introduction")
            })
        )

        assertEquals (
            crossProduct (
                extent (students),
                selection (extent (courses), (c: Rep[Course]) => c.title.startsWith ("Introduction"))
            ),
            query
        )
    }

    @Test
    def testCrossProduct2Selection1stAnd2nd () {
        val query = plan (
            SELECT (*) FROM(students, courses) WHERE ((s: Rep[Student], c: Rep[Course]) => {
                s.firstName == "Sally" &&
                    c.title.startsWith ("Introduction")
            })
        )

        assertEquals (
            crossProduct (
                selection (extent (students), (s: Rep[Student]) => s.firstName == "Sally"),
                selection (extent (courses), (c: Rep[Course]) => c.title.startsWith ("Introduction"))
            ),
            query
        )
    }

    @Test
    def testCrossProduct2Selections1stAnd2ndInterleaved () {
        val query = plan (
            SELECT (*) FROM(students, courses) WHERE ((s: Rep[Student], c: Rep[Course]) => {
                s.firstName == "Sally" &&
                    c.title.startsWith ("Introduction") &&
                    s.lastName == "Fields"
            })
        )

        assertEquals (
            crossProduct (
                selection (extent (students), (s: Rep[Student]) => s.firstName == "Sally" && s.lastName == "Fields"),
                selection (extent (courses), (c: Rep[Course]) => c.title.startsWith ("Introduction"))
            ),
            query
        )
    }

    @Test
    def testCrossProduct2Selection1stAnd2ndCompared () {
        val query = plan (
            SELECT (*) FROM(students, courses) WHERE ((s: Rep[Student], c: Rep[Course]) => {
                s.firstName != c.title
            })
        )

        assertEquals (
            selection (
                crossProduct (
                    extent (students),
                    extent (courses)
                ),
                (e: Rep[(Student, Course)]) => {
                    e._1.firstName != e._2.title
                }
            ),
            query
        )
    }

    @Test
    def testJoin2 () {
        val query = plan (
            SELECT (*) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber
            })
        )

        assertEquals (
            equiJoin (
                extent (students),
                extent (registrations),
                scala.Seq ((
                    fun ((s: Rep[Student]) => s.matriculationNumber),
                    fun ((r: Rep[Registration]) => r.studentMatriculationNumber)
                    ))
            ),
            query
        )
    }

    @Test
    def testJoin2Projection () {
        val query = plan (
            SELECT ((r: Rep[Registration], c: Rep[Course]) => c.creditPoints) FROM(registrations, courses) WHERE (
                (r: Rep[Registration], c: Rep[Course]) => r.courseNumber == c.number
                )
        )

        assertEquals (
            projection (
                equiJoin (
                    extent (registrations),
                    extent (courses),
                    scala.Seq ((
                        fun ((r: Rep[Registration]) => r.courseNumber),
                        fun ((c: Rep[Course]) => c.number)
                        ))
                ),
                fun ((r: Rep[Registration], c: Rep[Course]) => c.creditPoints)
            ),
            query
        )
    }


    @Test
    def testJoin2SelectionBoth () {
        val query = plan (
            SELECT (*) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber &&
                    s.firstName == "Sally" &&
                    r.courseNumber == 12345
            })
        )

        assertEquals (
            equiJoin (
                selection (extent (students), (s: Rep[Student]) => s.firstName == "Sally"),
                selection (extent (registrations), (r: Rep[Registration]) => r.courseNumber == 12345),
                scala.Seq ((
                    fun ((s: Rep[Student]) => s.matriculationNumber),
                    fun ((r: Rep[Registration]) => r.studentMatriculationNumber)
                    ))
            ),
            query
        )
    }

    @Test
    def testJoin2SelectionBothAndCompare () {
        val query = plan (
            SELECT (*) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber &&
                    s.lastName != r.comment &&
                    s.firstName == "Sally" &&
                    r.courseNumber == 12345
            })
        )

        assertEquals (
            selection (
                equiJoin (
                    selection (extent (students), (s: Rep[Student]) => s.firstName == "Sally"),
                    selection (extent (registrations), (r: Rep[Registration]) => r.courseNumber == 12345),
                    scala.Seq ((
                        fun ((s: Rep[Student]) => s.matriculationNumber),
                        fun ((r: Rep[Registration]) => r.studentMatriculationNumber)
                        ))
                ),
                (s: Rep[Student], r: Rep[Registration]) => {
                    s.lastName != r.comment
                }
            ),
            query
        )
    }

    @Test
    def testDistinctExtents () {
        val query = plan (
            SELECT DISTINCT (*) FROM(students, registrations)
        )

        assertEquals (
            duplicateElimination (
                crossProduct (
                    extent (students),
                    extent (registrations)
                )
            ),
            query
        )
    }

    @Ignore
    @Test
    def testJoin2CountStar () {
        val query = plan (
            SELECT (COUNT (*)) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber
            })
        )
    }

    @Ignore
    @Test
    def testJoin2AggregateGroup1 () {

        val query = plan (
            SELECT ((s: Rep[String]) => s) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber
            }) GROUP BY( (s: Rep[Student], r: Rep[Registration]) => s.lastName)
        )

    }

    @Ignore
    @Test
    def testJoin2SumCreditPoints () {
        val query = plan (
            SELECT (SUM ((r: Rep[Registration], c: Rep[Course]) => c.creditPoints)) FROM(registrations, courses) WHERE (
                (r: Rep[Registration], c: Rep[Course]) => r.courseNumber == c.number
                )
        )
    }


}
