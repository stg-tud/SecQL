package unisson.query.code_model

import de.tud.cs.st.vespucci.interfaces.ICodeElement
import de.tud.cs.st.bat.resolved.{Type, ObjectType}
import soot.bridge.ISootCodeElement
import sae.bytecode.structure.{FieldInfo, MethodInfo}

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.12.11
 * Time: 14:07
 *
 */
trait SourceElement[+T <: AnyRef]
    extends ICodeElement
{
    def element: T
}

object SourceElement
{
    def apply[T <: AnyRef](element: T): SourceElement[_ <: AnyRef] = {
        if (element.isInstanceOf[ObjectType]) {
            return new ClassTypeAdapter (element.asInstanceOf[ObjectType])
        }
        if (element.isInstanceOf[MethodInfo]) {
            return new MethodInfoAdapter (element.asInstanceOf[MethodInfo])
        }
        if (element.isInstanceOf[FieldInfo]) {
            return new FieldInfoAdapter (element.asInstanceOf[FieldInfo])
        }
        if (element.isInstanceOf[Type]) {
            return new TypeReference (element.asInstanceOf[Type])
        }

        throw new IllegalArgumentException ("can not convert " + element + " to a SourceElement")
    }

    def unapply[T <: AnyRef](sourceElement: SourceElement[T]): Option[T] = {
        Some (sourceElement.element)
    }

    // TODO careful with to string, use for testing only
    implicit def compare[T <: AnyRef](x: SourceElement[T], y: SourceElement[T]): Int = {
        if (x.isInstanceOf[ClassTypeAdapter] && y.isInstanceOf[ClassTypeAdapter]) {
            return x.asInstanceOf[ClassTypeAdapter].getTypeQualifier
                .compare (y.asInstanceOf[ClassTypeAdapter].getTypeQualifier)
        }

        x.toString.compareTo (y.toString)
    }

    implicit def ordering[T <: AnyRef]: Ordering[SourceElement[T]] = new Ordering[SourceElement[T]] {
        def compare(x: SourceElement[T], y: SourceElement[T]) = SourceElement.compare (x, y)
    }
}