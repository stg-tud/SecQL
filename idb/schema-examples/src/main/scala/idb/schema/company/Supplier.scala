package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

case class Supplier(id : Int, name : String, city : String)
	extends Nameable

trait SupplierSchema
	extends NameableSchema {

	val IR: StructExp

	import IR._

	case class SupplierInfixOp (p: Rep[Supplier]) {
		def city: Rep[String] = field[String](p, "city")
	}

	implicit def supplierToInfixOp (p: Rep[Supplier]) : SupplierInfixOp =
		SupplierInfixOp (p)

}