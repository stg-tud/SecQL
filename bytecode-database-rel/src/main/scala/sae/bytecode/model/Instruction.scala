package sae
package bytecode
package model

/**
 * Instruction was already taken as a name in bat and I wanted to avoid 
 * name clashes in wildcard imports
 */
trait Instr[T] {
    val declaringMethod: Method

    val programCounter: Int

    type Kind = T
}