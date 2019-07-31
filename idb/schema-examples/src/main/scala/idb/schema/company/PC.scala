package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

case class PC(productId : Int, componentId : Int, quantity : Int)

trait PCSchema {
	val IR: StructExp

	import IR._

	case class PCInfixOp (p: Rep[PC])
	{

		def productId: Rep[Int] = field[Int](p, "productId")

		def componentId: Rep[Int] = field[Int](p, "componentId")

		def quantity: Rep[Int] = field[Int](p, "quantity")

	}

	implicit def pcToInfixOp (p: Rep[PC]) : PCInfixOp =
		PCInfixOp (p)
}