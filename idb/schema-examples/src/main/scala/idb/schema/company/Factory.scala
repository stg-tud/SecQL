package idb.schema.company

import scala.language.implicitConversions
import scala.virtualization.lms.common.StructExp

case class Factory(id : Int, city : String)

trait FactorySchema {
	val IR: StructExp

	import IR._

	case class FactoryInfixOp (p: Rep[Factory])
	{

		def id: Rep[Int] = field[Int](p, "id")

		def city: Rep[String] = field[String](p, "city")

	}

	implicit def factoryToInfixOp (p: Rep[Factory]) : FactoryInfixOp =
		FactoryInfixOp (p)
}