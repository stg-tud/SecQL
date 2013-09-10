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
package sae.bytecode.asm

import idb.SetExtent
import sae.bytecode.structure.instructions.{BytecodeInstructionsManifest, BytecodeInstructionsRelations}
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.bytecode.asm.instructions.opcodes.{TABLESWITCH, LOOKUPSWITCH, RET, IINC}

/**
 *
 * @author Ralf Mitschke
 */
trait ASMInstructionsRelations
    extends ASMInstructions
    with BytecodeInstructionsManifest
    with BytecodeInstructionsRelations
{

    val basicInstructions =
        SetExtent.empty[Instruction]()

    val fieldReadInstructions =
        SetExtent.empty[FieldAccessInstruction]()

    val fieldWriteInstructions =
        SetExtent.empty[FieldAccessInstruction]()

    val constantValueInstructions =
        SetExtent.empty[ConstantValueInstruction[Any]]()

    val lookupSwitchInstructions =
        SetExtent.empty[LOOKUPSWITCH]()

    val tableSwitchInstructions =
        SetExtent.empty[TABLESWITCH]()

    val methodInvocationInstructions =
        SetExtent.empty[MethodInvocationInstruction]()

    val objectTypeInstructions =
        SetExtent.empty[ObjectTypeInstruction]()

    val newArrayInstructions =
        SetExtent.empty[NewArrayInstruction[Any]]()

    val localVariableLoadInstructions =
        SetExtent.empty[LocalVariableAccessInstruction]()

    val localVariableStoreInstructions =
        SetExtent.empty[LocalVariableAccessInstruction]()

    val retInstructions =
        SetExtent.empty[RET]()

    val integerIncrementInstructions =
        SetExtent.empty[IINC]()

    val conditionalJumpInstructions =
        SetExtent.empty[JumpInstruction]()

    val unconditionalJumpInstructions =
        SetExtent.empty[JumpInstruction]()


    lazy val jumpInstructions = compile (
        conditionalJumpInstructions UNION ALL (unconditionalJumpInstructions)
    )


    lazy val fieldAccessInstructions = compile(
        fieldReadInstructions UNION ALL (fieldWriteInstructions)
    )


    lazy val localVariableAccessInstructions = compile (
        localVariableLoadInstructions UNION ALL
            (localVariableStoreInstructions) UNION ALL
            (extent (integerIncrementInstructions).asInstanceOf[Rep[Query[LocalVariableAccessInstruction]]]) UNION ALL
            (extent (retInstructions).asInstanceOf[Rep[Query[LocalVariableAccessInstruction]]])
    )


    lazy val instructions =
        basicInstructions UNION ALL
        (fieldAccessInstructions)UNION ALL
        (fieldAccessInstructions)


}