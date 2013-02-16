package sandbox.stackAnalysis.datastructure

import de.tud.cs.st.bat.resolved.Type
import java.util
import sae.operators.Combinable


/**
 * Objects of this class are the local variable stores in states of the machine. LocVariables objects are immutable.
 * @param varStore The store where the variables are stored. Note that this should not be changed.
 */
case class LocVariables(varStore: Array[Item])(implicit m: Manifest[Item]) extends Combinable[LocVariables] {

  def apply(index: Int): Item =
    if (varStore(index) == null)
      Item.createNoneItem
    else
      varStore(index)

  private def setVar(index: Int, size: Int, variable: Item): LocVariables = {
    val resV: Array[Item] = Array.ofDim[Item](varStore.length)

    Array.copy(varStore, 0, resV, 0, varStore.length)

    resV(index) = variable
    for (i <- 1 until size)
      resV(index + i) = Item.createContinue(variable)

    new LocVariables(resV)
  }

  def setVar(index: Int, t: Item): LocVariables = {
    setVar(index, t.size, t)
  }

  def setVar(index: Int, t: Type, pc: Int): LocVariables =
    setVar(index, t.computationalType.operandSize, Item.createItem(ItemType.fromType(t), pc))


  def length(): Int = {
    varStore.length
  }

  def combineWith(other: LocVariables): LocVariables = {
    if (other == null)
      this
    else {

      val res: Array[Item] = Array.ofDim[Item](length)

      for (i <- 0 until length) {
        if (varStore(i) == null)
          res(i) = other.varStore(i)
        else
          res(i) = varStore(i).combineWith(other.varStore(i))
      }

      LocVariables(res)
    }
  }


  def equals(other: LocVariables): Boolean = {
    if (length() != other.length())
      return false

    for (i <- 0 until length()) {
      if (varStore(i) == null)
        return other.varStore(i) == null
      if (!varStore(i).equals(other.varStore(i)))
        return false
    }

    return true
  }

  override def hashCode(): Int =
    util.Arrays.hashCode(varStore.asInstanceOf[Array[AnyRef]])


  override def toString(): String = {
    return varStore.mkString("LocalVars<" + varStore.length + ">: [", " || ", "]")
  }

}