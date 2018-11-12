package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

case class FP(factoryId : Int, productId : Int)

trait FPSchema {
	val IR: StructExp

	import IR._

	case class FPInfixOp (p: Rep[FP])
	{
		def factoryId: Rep[Int] = field[Int](p, "factoryId")

		def productId: Rep[Int] = field[Int](p, "productId")
	}

	implicit def fpToInfixOp (p: Rep[FP]) : FPInfixOp =
		FPInfixOp (p)
}