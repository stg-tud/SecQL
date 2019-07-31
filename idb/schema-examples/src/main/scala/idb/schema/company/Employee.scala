package idb.schema.company

case class Employee(id : Int, name : String)
	extends Nameable

trait EmployeeSchema
	extends NameableSchema