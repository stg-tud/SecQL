package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

case class Component(id : Int, name : String, material : String)
	extends Nameable

trait ComponentSchema
	extends NameableSchema {

	val IR: StructExp

	import IR._

	case class ComponentInfixOp (p: Rep[Component]) {
		def material: Rep[String] = field[String](p, "material")
	}

	implicit def componentToInfixOp (p: Rep[Component]) : ComponentInfixOp =
		ComponentInfixOp (p)

}