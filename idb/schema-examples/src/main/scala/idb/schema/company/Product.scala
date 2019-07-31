package idb.schema.company

case class Product(id : Int, name : String)
	extends Nameable

trait ProductSchema
	extends NameableSchema