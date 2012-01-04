package unisson.query.code_model

import sae.bytecode.model.Method
import de.tud.cs.st.vespucci.interfaces.IMethodDeclaration


/**
 *
 * Author: Ralf Mitschke
 * Date: 11.12.11
 * Time: 13:13
 *
 */
class MethodDeclaration(val element: Method)
        extends IMethodDeclaration with SourceElement[Method]
{

    def getPackageIdentifier = element.declaringRef.packageName

    def getSimpleClassName = element.declaringRef.simpleName

    def getReturnTypeQualifier = element.returnType.signature

    lazy val getParameterTypeQualifiers = element.parameters.map(_.signature).toArray

    def getLineNumber = -1

    def getMethodName = element.name

    override def hashCode() = element.hashCode()

    override def equals(obj: Any) : Boolean = {
        if( !obj.isInstanceOf[MethodDeclaration] ){
            return false
        }
        element.equals(obj.asInstanceOf[MethodDeclaration].element)
    }

    override def toString = element.declaringRef.signature +
            element.name +
            "(" + (
            if (getParameterTypeQualifiers.isEmpty) {
                ""
            }
            else {
                getParameterTypeQualifiers.reduceLeft(_ + "," + _)
            }
            ) + ")" +
            ":" + element.returnType.signature
}