package idb.syntax.iql

import akka.actor.ActorPath
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import idb.algebra.demo.RelationalAlgebraDemoPrintPlan
import idb.algebra.ir._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.algebra.remote.PlacementStrategy
import idb.lms.extensions.{FunctionUtils, ScalaOpsPkgExpExtensions}
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.remote.stream.StreamAdapter
import idb.syntax.iql
import idb.syntax.iql.IR._
import idb.syntax.iql.runtime.{CompilerBinding, RemoteUtils}

/**
  * Syntax operator to define the deployment target host of the root of a distributed query. The result relation will be
  * located on that host while the remaining operator tree may be distributed over other nodes.
  */
object ROOT {

	def UNSAFE[Domain: Manifest](host: ActorPath, query: Rep[Query[Domain]])(implicit env: QueryEnvironment): Relation[Domain] = {
		val relation: Relation[Domain] = query
		UNSAFE(host, relation)
	}

	def UNSAFE[Domain: Manifest](host: ActorPath, relation: Relation[Domain])(implicit env: QueryEnvironment): Relation[Domain] = {
		val controllerRef = RemoteUtils.deployOperator(env.system, host)(relation)
		RemoteUtils.deployReceiver(env.system, controllerRef)
	}

	def UNSAFE[Domain: Manifest](host: RemoteHost, query: Rep[Query[Domain]])(implicit env: QueryEnvironment): Relation[Domain] =
		UNSAFE(host.path, query)


	def UNSAFE[Domain: Manifest](host: RemoteHost, relation: Relation[Domain])(implicit env: QueryEnvironment): Relation[Domain] =
		UNSAFE(host.path, relation)

	def apply[Domain: Manifest](rootHost: RemoteHost, query: Rep[Query[Domain]], placement: String = "unnamed-placement")(implicit env: QueryEnvironment): Relation[Domain] = {

		object Placement extends PlacementStrategy {
			val IR = idb.syntax.iql.IR
			override val placementId: String = placement
		}
		val transformedQuery = Placement.transform(root(query, rootHost))

		val printer = new RelationalAlgebraDemoPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		Predef.println()
		Predef.println()
		Predef.println()
		Predef.println()
		Predef.println("***********************************************")
		Predef.println("*** PLACED QUERY ******************************")
		Predef.println("***********************************************")
		Predef.println()

		Predef.println(printer.quoteRelation(transformedQuery))

		Predef.println()
		Predef.println("***********************************************")
		Predef.println("***********************************************")
		Predef.println()
		Predef.println()
		Predef.println()
		Predef.println()

		val relation: Relation[Domain] = transformedQuery
		val RemoteHost(_, queryPath) = transformedQuery.host

		implicit val mat: ActorMaterializer = ActorMaterializer()(env.system)
		val rootOperator = RemoteUtils.deployOperator(env.system, queryPath)(relation)
		val r = RemoteUtils.deployReceiver(env.system, rootOperator)
		r.source.runWith(Sink.foreach(msg => StreamAdapter.toObserver(msg, r)))
		r
	}

	def apply[Domain: Manifest](query: Rep[Query[Domain]])(implicit env: QueryEnvironment): Relation[Domain] = {
		val r = compile(query)
		CompilerBinding.initialize(r)
		r
	}
}
