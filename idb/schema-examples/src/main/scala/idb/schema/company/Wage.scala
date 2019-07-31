package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

case class Wage(employeeId : Int, wagePerHour : Double, hoursPerMonth : Double)

trait WageSchema {
	val IR: StructExp

	import IR._

	case class WageInfixOp (p: Rep[Wage])
	{

		def employeeId: Rep[Int] = field[Int](p, "employeeId")

		def wagePerHour: Rep[Double] = field[Double](p, "wagePerHour")

		def hoursPerMonth: Rep[Double] = field[Double](p, "hoursPerMonth")

	}

	implicit def wageToInfixOp (p: Rep[Wage]) : WageInfixOp =
		WageInfixOp (p)
}