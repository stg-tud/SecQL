package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

/**
  * Factory-Employee
  */
case class FE(factoryId : Int, employeeId : Int, job : String)

trait FESchema {
	val IR: StructExp

	import IR._

	case class FEInfixOp (p: Rep[FE])
	{

		def factoryId: Rep[Int] = field[Int](p, "factoryId")

		def employeeId: Rep[Int] = field[Int](p, "employeeId")

		def job: Rep[String] = field[String](p, "job")

	}

	implicit def feToInfixOp (p: Rep[FE]) : FEInfixOp =
		FEInfixOp (p)
}