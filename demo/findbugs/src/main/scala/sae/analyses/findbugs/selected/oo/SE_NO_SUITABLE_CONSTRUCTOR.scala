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
package sae.analyses.findbugs.selected.oo

import sae.bytecode._
import sae.Relation
import sae.analyses.findbugs.base.oo.Definitions
import sae.syntax.sql._
import de.tud.cs.st.bat.resolved.ObjectType
import structure.{ClassDeclaration, InheritanceRelation}

/**
 *
 * @author Ralf Mitschke
 *
 */

object SE_NO_SUITABLE_CONSTRUCTOR
    extends (BytecodeDatabase => Relation[ObjectType])
{
    def apply(database: BytecodeDatabase): Relation[ObjectType] = {
        val definitions = Definitions (database)
        import database._
        import definitions._


        val superClassesOfSerializableClasses: Relation[ObjectType] = SELECT ((i: InheritanceRelation, o: ObjectType) => i.superType) FROM (classInheritance, subTypesOfSerializable) WHERE
            (subType === identity[ObjectType] _)

        val directlySerializable: Relation[ClassDeclaration] =
            SELECT (*) FROM classDeclarations WHERE
                (!_.isInterface) AND
                (_.interfaces.contains (serializable)) AND
                (_.superClass.isDefined) AND EXISTS (
                SELECT (*) FROM classDeclarations WHERE (!_.isInterface) AND (classType === superClass)
            )

        SELECT DISTINCT  ((_:ClassDeclaration).superClass.get) FROM directlySerializable WHERE NOT (
            EXISTS (SELECT (*) FROM constructors WHERE (_.parameterTypes == Nil) AND (declaringType === superClass))
        )

    }

}
