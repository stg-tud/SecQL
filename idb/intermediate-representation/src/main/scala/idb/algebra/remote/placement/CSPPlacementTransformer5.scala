package idb.algebra.remote.placement

import idb.algebra.QueryTransformerAdapter
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.exceptions.NoServerAvailableException
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.RemoteUtils
import idb.metrics.PlacementStatistics
import idb.metrics.model._
import idb.query.{Host, QueryEnvironment}

import scala.collection.mutable

trait CSPPlacementTransformer5
	extends QueryTransformerAdapter with QueryTaint {
	//Defines whether the query tree should be use fragments bigger then single operators
	val USE_PRIVACY: Boolean = true
	// If true, total cost is network cost * load coast, otherwise it is independent sum with network priority
	val TOTAL_COST_PRODUCT: Boolean = true
	// If TOTAL_COST_PRODUCT is false, this weight is applied on the network cost (weight for load cost is 1)
	val NETWORK_COST_WEIGHT: Double = 1000D
	// If true, SeqQL host load cost is sum(operator load/host capacity),
	// otherwise sum((sum(host operators load)/host capacity) ^2)
	val SECQL_LOAD_COST: Boolean = false
	// If false, input data dependent operator load values are chosen, otherwise static SecQL values
	val SECQL_OPERATOR_LOADS: Boolean = false
	// CSP solving timeout in seconds
	val TIMEOUT = 30

	val placementId: String

	val IR: RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RemoteUtils


	import IR._

	var nextOperatorId = 0

	object Operator {

		/**
		  * Transforms query relation and host id map into Operator instances recursively. Returns the operator tree
		  * root, from which descendant operators are referenced recursively.
		  *
		  * @param query
		  * @param hostId
		  * @return
		  */
		def apply(query: IR.Rep[IR.Query[_]], hostId: Map[Host, Int]): Operator =
			query match {
				//Base
				case QueryTable(_, _, _, h) =>
					Operator(query, Some(hostId(h)), Seq.empty)
				case QueryRelation(_, _, _, h) =>
					Operator(query, Some(hostId(h)), Seq.empty)
				case Def(Root(r, h)) =>
					Operator(query, Some(hostId(h)), Seq(Operator(r, hostId)))
				case Def(Materialize(r)) =>
					Operator(query, None, Seq(Operator(r, hostId)))

				//Basic Operators
				case Def(Selection(r, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))
				case Def(Projection(r, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))
				case Def(CrossProduct(r1, r2)) =>
					val c1 = Operator(r1, hostId)
					val c2 = Operator(r2, hostId)
					Operator(query, None, Seq(c1, c2))
				case Def(EquiJoin(r1, r2, _)) =>
					val c1 = Operator(r1, hostId)
					val c2 = Operator(r2, hostId)
					Operator(query, None, Seq(c1, c2))
				case Def(DuplicateElimination(r)) =>
					Operator(query, None, Seq(Operator(r, hostId)))
				case Def(Unnest(r, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))

				//Set theory operators
				case Def(UnionAdd(r1, r2)) =>
					val c1 = Operator(r1, hostId)
					val c2 = Operator(r2, hostId)
					Operator(query, None, Seq(c1, c2))
				case Def(UnionMax(r1, r2)) =>
					val c1 = Operator(r1, hostId)
					val c2 = Operator(r2, hostId)
					Operator(query, None, Seq(c1, c2))
				case Def(Intersection(r1, r2)) =>
					val c1 = Operator(r1, hostId)
					val c2 = Operator(r2, hostId)
					Operator(query, None, Seq(c1, c2))
				case Def(Difference(r1, r2)) =>
					val c1 = Operator(r1, hostId)
					val c2 = Operator(r2, hostId)
					Operator(query, None, Seq(c1, c2))

				//Aggregation operators
				case Def(AggregationSelfMaintained(r, _, _, _, _, _, _, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))
				case Def(AggregationNotSelfMaintained(r, _, _, _, _, _, _, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))

				//Remote
				case Def(Reclassification(r, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))
				case Def(Declassification(r, _)) =>
					Operator(query, None, Seq(Operator(r, hostId)))
				case Def(ActorDef(_, h, _)) =>
					Operator(query, Some(hostId(h)), Seq.empty)
			}
	}

	case class Operator(
						   query: IR.Rep[IR.Query[_]],
						   pinnedTo: Option[Int],
						   children: Seq[Operator]
					   ) {
		val id: Int = nextOperatorId
		nextOperatorId += 1

		lazy val selectivity: Double =
			if (selectivityLib != null) {
				selectivityLib(query.hashCode)
			}
			else
				query match {
					//Base
					case QueryTable(_, _, _, _) => 1d
					case QueryRelation(_, _, _, _) => 1d
					case Def(Root(_, _)) => 1d
					case Def(Materialize(_)) => 1d

					//Basic Operators
					case Def(Selection(_, _)) => 0.5d
					case Def(Projection(_, _)) => 1d
					case Def(CrossProduct(_, _)) =>
						val c1 = children.head
						val c2 = children(1)
						(c1.outgoingLink * c2.outgoingLink) / (c1.outgoingLink + c2.outgoingLink)
					case Def(EquiJoin(_, _, _)) =>
						val c1 = children.head
						val c2 = children(1)
						2 * Math.min(c1.outgoingLink, c2.outgoingLink) / (c1.outgoingLink + c2.outgoingLink)
					case Def(DuplicateElimination(_)) => 0.5d
					case Def(Unnest(_, _)) => 5d

					//Set theory operators
					case Def(UnionAdd(_, _)) => 1d
					case Def(UnionMax(_, _)) => 1d
					case Def(Intersection(_, _)) => 0.5d
					case Def(Difference(_, _)) => 0.5d

					//Aggregation operators
					case Def(AggregationSelfMaintained(_, _, _, _, _, _, _, _)) => 2d
					case Def(AggregationNotSelfMaintained(_, _, _, _, _, _, _, _)) => 2d

					//Remote
					case Def(Reclassification(_, _)) => 1d
					case Def(Declassification(_, _)) => 1d
					case Def(ActorDef(_, _, _)) => 1d
				}

		lazy val load: Double =
			query match {
				//Base
				case QueryTable(_, _, _, _) => 0d
				case QueryRelation(_, _, _, _) => 0d
				case Def(Root(_, _)) => 0d
				case Def(Materialize(_)) =>
					if (SECQL_OPERATOR_LOADS) 2d else 2d * children.head.outgoingLink

				//Basic Operators
				case Def(Selection(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 1d else 1d * children.head.outgoingLink
				case Def(Projection(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 1d else 1d * children.head.outgoingLink
				case Def(CrossProduct(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 8d else children.map(_.outgoingLink).product
				case Def(EquiJoin(_, _, _)) =>
					if (SECQL_OPERATOR_LOADS) 4d else (2d + selectivity) * children.map(_.outgoingLink).sum
				case Def(DuplicateElimination(_)) =>
					if (SECQL_OPERATOR_LOADS) 2d else 2d * children.head.outgoingLink
				case Def(Unnest(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 1d else 1d * children.head.outgoingLink

				//Set theory operators
				case Def(UnionAdd(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 1d else 1d * children.head.outgoingLink
				case Def(UnionMax(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 4d else 2d * children.head.outgoingLink
				case Def(Intersection(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 4d else 2d * children.head.outgoingLink
				case Def(Difference(_, _)) =>
					if (SECQL_OPERATOR_LOADS) 4d else 2d * children.head.outgoingLink

				//Aggregation operators
				case Def(AggregationSelfMaintained(_, _, _, _, _, _, _, _)) =>
					if (SECQL_OPERATOR_LOADS) 3d else (2d + selectivity) * children.map(_.outgoingLink).sum
				case Def(AggregationNotSelfMaintained(_, _, _, _, _, _, _, _)) =>
					if (SECQL_OPERATOR_LOADS) 3d else (2d + selectivity) * children.map(_.outgoingLink).sum

				//Remote
				case Def(Reclassification(_, _)) => 0d
				case Def(Declassification(_, _)) => 0d
				case Def(ActorDef(_, _, _)) => 0d
			}

		lazy val name: String =
			query match {
				//Base
				case QueryTable(_, _, _, _) => "query-table"
				case QueryRelation(_, _, _, _) => "query-relation"
				case Def(Root(_, _)) => "root"
				case Def(Materialize(_)) => "materialize"

				//Basic Operators
				case Def(Selection(_, _)) => "selection"
				case Def(Projection(_, _)) => "projection"
				case Def(CrossProduct(_, _)) => "cross-product"
				case Def(EquiJoin(_, _, _)) => "equi-join"
				case Def(DuplicateElimination(_)) => "duplicate-elimination"
				case Def(Unnest(_, _)) => "unnest"

				//Set theory operators
				case Def(UnionAdd(_, _)) => "union-add"
				case Def(UnionMax(_, _)) => "union-max"
				case Def(Intersection(_, _)) => "intersection"
				case Def(Difference(_, _)) => "difference"

				//Aggregation operators
				case Def(AggregationSelfMaintained(_, _, _, _, _, _, _, _)) => "aggregation (sm)"
				case Def(AggregationNotSelfMaintained(_, _, _, _, _, _, _, _)) => "aggreation (not sm)"

				//Remote
				case Def(Reclassification(_, _)) => "re-classification"
				case Def(Declassification(_, _)) => "de-classification"
				case Def(ActorDef(_, _, _)) => "actor"
			}

		lazy val outgoingLink: Double =
			if (children.isEmpty) selectivity * 1000
			else children.map(_.outgoingLink).sum * selectivity

		lazy val toList: Seq[Operator] =
			Seq(this) ++ children.flatMap(child => child.toList)

		override def toString: String = s"($id, query: ${query.hashCode}, load $load, selectivity $selectivity, pinnedTo $pinnedTo, $query, children ${children.map(_.id)})\n"
	}

	case class Link(
					   sender: Int,
					   receiver: Int,
					   load: Double
				   ) {
		override def toString: String = s"($sender => $receiver: $load)\n"
	}

	/**
	  * Maps the hashcode of an operator to it's selectivity. If 0, default values are used. If not null and an
	  * operator is initialized that is not mentioned in the Map, a NoSuchElementException is thrown
	  */
	var selectivityLib: Map[Int, Float] = _

	override def transform[Domain: Manifest](relation: Rep[Query[Domain]])(implicit env: QueryEnvironment): Rep[Query[Domain]] = {

		println("REL" + relation.hashCode())

		//		println("global Defs = ")
		//		IR.globalDefsCache.toList.sortBy(t => t._1.id).foreach(println)

		// Init selectivity lib
		selectivityLib =
			if (SelectivityLib2.libs.contains(placementId)) {
				println(s"Using predefined selectivity lib for placement $placementId")
				SelectivityLib2.libs(placementId)
			}
			else {
				println(s"Using default selectivity values for placement $placementId")
				null
			}

		val hostList = env.hosts.toSeq
		val hostId = hostList.zipWithIndex.toMap
		val hostCapacity = hostList.map(env.priorityOf)
		//Prepare data for CSP Solver function
		val operatorTree: Operator = Operator(relation, hostId)
		val operators: Seq[Operator] = operatorTree.toList.sortBy(_.id)
		// Make sure ids of operators are correct
		operators.zipWithIndex foreach { t =>
			val (operator, id) = t
			if (id != operator.id)
				throw new RuntimeException("Operator ID mismatch")
		}

		val operatorHosts: Seq[Seq[Int]] = operators.map {
			operator =>
				if (USE_PRIVACY)
					env.findHostsFor(taintOf(operator.query).ids).map(h => hostId(h)).toSeq
				else
					hostId.values.toSeq
		}

		val links: Seq[Link] = operators.flatMap { operator =>
			operator.children.map { childOperator =>
				Link(childOperator.id, operator.id, childOperator.outgoingLink)
			}
		}


		//Compute placement using the CSP solver
		val placement: Seq[Int] = computePlacement(operators, operatorHosts, links, hostCapacity, hostList)

		if (placement == null)
			throw new NoServerAvailableException()


		//Translate results back to the AST
		implicit val placementMap: Map[IR.Rep[IR.Query[_]], Host] =
			placement.zipWithIndex.map { t =>
				val (hostId, operatorId) = t
				val operator = operators(operatorId)
				(operator.query, hostList(hostId))
			}.toMap

		println("placement Map = ")
		placementMap.toList.foreach(println)

		super.transform(addRemotes(relation))
	}

	private def computePlacement(
									operators: Seq[Operator],
									operatorHostCandidates: Seq[Seq[Int]],
									links: Seq[Link],
									hostCapacity: Seq[Int],
									hosts: Seq[Host]
								): Seq[Int] = {
		import org.jacop.constraints._
		import org.jacop.core._
		import org.jacop.floats.constraints._
		import org.jacop.floats.core._
		import org.jacop.search._

		println("Input: ")
		println("operators: " + operators)
		println("operator hosts: " + operatorHostCandidates)
		println("links: " + links)
		println("capacities: " + hostCapacity)

		val startTime = System.nanoTime()


		val numOperators = operators.size
		val numLinks = links.size
		val numHosts = hostCapacity.size

		//Create global store
		val store = new Store()

		// Host on which the operator is placed on (operator -> host)
		val operatorHost = new Array[IntVar](numOperators)
		// Operator load (operator -> load)
		val operatorLoad = new Array[Double](numOperators)
		operators.zipWithIndex foreach { t =>
			val (operator, operatorId) = t
			operatorHost(operatorId) = new IntVar(store, s"operator${operatorId}host", 0, numHosts - 1)

			// Add host placement constraints
			// Pin operator on host if needed
			operator.pinnedTo match {
				case Some(hostId) => store.impose(new XeqC(operatorHost(operatorId), hostId))
				case None =>
			}
			// Add invalid operator on host placement constraints
			(0 until numHosts) foreach { hostId =>
				if (!operatorHostCandidates(operatorId).contains(hostId))
					store.impose(new XneqC(operatorHost(operatorId), hostId))
			}

			// Define operator load
			operatorLoad(operatorId) = operator.load
		}

		// Load on each host  (sum of operator loads, which are placed on the host)
		val hostLoad = new Array[FloatVar](numHosts)
		// Cost value of the load on a host, normalized to [0, 1]
		val hostLoadCostNormed = new Array[FloatVar](numHosts)
		(0 until numHosts) foreach { host =>
			// Init host load (constraints are defined in bin-packing)
			hostLoad(host) = new FloatVar(store, s"host${host}load", 0, operatorLoad.sum)

			// Define load cost per host
			if (SECQL_LOAD_COST) {
				// Domain [0, operator load sum], because capacity must be >= 1
				val hostLoadCost = new FloatVar(store, s"host${host}loadCost" + host, 0, operatorLoad.sum / hostCapacity.min)
				// host load cost = host operators load / host capacity
				store.impose(new PdivCeqR(hostLoad(host), hostCapacity(host), hostLoadCost))

				// Norm cost to be in [0, 1] (even after summation of all host costs)
				hostLoadCostNormed(host) = new FloatVar(store, s"host${host}loadCostNormed" + host, 0, 1)
				store.impose(new PmulCeqR(hostLoadCost, hostCapacity.min.toDouble / operatorLoad.sum, hostLoadCostNormed(host)))
			}
			else {
				val hostLoadQuotient = new FloatVar(store, s"host${host}loadQuotient", 0, operatorLoad.sum / hostCapacity(host))
				// host load quotient = host operators load / host capacity, [0, operator load sum / host capacity <= operator load sum]
				store.impose(new PdivCeqR(hostLoad(host), hostCapacity(host), hostLoadQuotient))

				// Domain [0, (operator load sum / host capacity)^2 <= operator load sum ^2], because capacity must be >= 1
				val hostLoadCost = new FloatVar(store, s"host${host}loadCost" + host, 0, Math.pow(operatorLoad.sum / hostCapacity(host), 2))
				// host load cost = host load quotient ^2
				store.impose(new PmulQeqR(hostLoadQuotient, hostLoadQuotient, hostLoadCost))

				// Norm cost to be in [0, 1] (even after summation of all host costs)
				hostLoadCostNormed(host) = new FloatVar(store, s"host${host}loadCostNormed" + host, 0, 1)
				store.impose(new PmulCeqR(hostLoadCost, Math.pow(hostCapacity.min.toDouble / operatorLoad.sum, 2), hostLoadCostNormed(host)))
			}
		}

		// Setup bin packing/operator to host assignment
		val placementLoads = new Array[Array[FloatVar]](numHosts)
		(0 until numHosts) foreach { host =>
			placementLoads(host) = new Array[FloatVar](numOperators)
			(0 until numOperators) foreach { operatorId =>
				placementLoads(host)(operatorId) = new FloatVar(store, s"host${host}Operator${operatorId}Load", 0, operatorLoad(operatorId))

				// If placements entry is 1, set load value in placementLoads
				store.impose(
					new IfThenElse(
						new XeqC(operatorHost(operatorId), host),
						new PeqC(placementLoads(host)(operatorId), operatorLoad(operatorId)),
						new PeqC(placementLoads(host)(operatorId), 0)
					)
				)
			}

			// Sum up all placed loads on a host => hostLoad
			store.impose(new SumFloat(placementLoads(host), "==", hostLoad(host)))
		}

		// Total load cost in [0, 1]
		val loadCost = new FloatVar(store, "loadCost", 0, 1)
		store.impose(new SumFloat(hostLoadCostNormed, "==", loadCost))

		// Load on each link via network
		val networkLinkLoad = new Array[FloatVar](numLinks)
		links.zipWithIndex foreach { t =>
			val (link, linkId) = t
			networkLinkLoad(linkId) = new FloatVar(store, s"networkLink${linkId}Load", 0, link.load)

			store.impose(
				new IfThenElse(
					new XeqY(
						operatorHost(link.sender),
						operatorHost(link.receiver)),
					new PeqC(networkLinkLoad(linkId), 0),
					new PeqC(networkLinkLoad(linkId), link.load)
				)
			)
		}

		// Sum of all link loads
		val linkLoadSum: Double = links.map(_.load).sum

		// Sum of all loads via network
		val networkLoad = new FloatVar(store, "networkLoad", 0, linkLoadSum)
		store.impose(new SumFloat(networkLinkLoad, "==", networkLoad))

		// Network cost as sum of network link load (Normalized to [0, 1])
		val networkCost = new FloatVar(store, "networkCost", 0, 1)
		store.impose(new PdivCeqR(networkLoad, linkLoadSum, networkCost))


		val cost: FloatVar =
			if (TOTAL_COST_PRODUCT) {
				// cost = load cost * network cost, [0, 1]
				val cost = new FloatVar(store, "cost", 0, 1)
				store.impose(new PmulQeqR(loadCost, networkCost, cost))

				cost
			}
			else {
				// Weight network cost
				val networkCostWeighted = new FloatVar(store, "networkCostWeighted", 0, NETWORK_COST_WEIGHT)
				store.impose(new PmulCeqR(networkCost, NETWORK_COST_WEIGHT, networkCostWeighted))

				// cost = load * network
				val cost = new FloatVar(store, "cost", 0, NETWORK_COST_WEIGHT + 1)
				store.impose(new PplusQeqR(loadCost, networkCostWeighted, cost))

				cost
			}

		val search: Search[IntVar] = new DepthFirstSearch[IntVar]()
		val placementStatistics = new SimpleSolutionListener[IntVar] with TimeOutListener {
			searchAll(true)
			recordSolutions(true)

			private var lowestCost: Double = Double.MaxValue
			private val bestSolutions = mutable.ArrayBuffer[PlacementSolution]()
			private var timedOut = false

			override def executeAfterSolution(search: Search[IntVar], select: SelectChoicePoint[IntVar]): Boolean = {
				val result = super.executeAfterSolution(search, select)

				val solutionId = solutionsNo() - 1
				val solutionVals = solutions(solutionId)
				val solutionCost = cost.value()
				if (solutionCost > lowestCost + 0.000000000000001) {
					throw new RuntimeException(s"Found new solution $solutionId (cost: $solutionCost), which is worse than the previous")
				}
				else if (solutionCost < lowestCost) {
					Predef.println(s"Found better solution $solutionId (cost: $solutionCost), resetting solution cache")
					bestSolutions.clear()
					lowestCost = solutionCost
				}
				else {
					Predef.println(s"Found additional solution $solutionId (cost: $solutionCost)")
				}

				bestSolutions.append(PlacementSolution(
					vars.map(_.index).zip(solutionVals.map(_.toString.toInt)).map { t =>
						val (operator, host) = t
						PlacementDecision(operator, host)
					}
				))

				result
			}

			/**
			  *
			  * @param duration Duration in milliseconds
			  * @return
			  */
			def generate(duration: Long): PlacementStatistics =
				PlacementStatistics(
					placementId,
					lowestCost,
					bestSolutions,
					bestSolutions.length - 1,
					operators.zip(operatorHostCandidates) map { t =>
						val (operator, candidateHosts) = t
						PlacementOperator(
							operator.id,
							operator.name,
							candidateHosts,
							operator.selectivity,
							operator.load,
							operator.outgoingLink)
					},
					links map { link =>
						PlacementLink(link.sender, link.receiver, link.load)
					},
					hosts.zipWithIndex map { t =>
						val (host, id) = t
						PlacementHost(id, host.name)
					},
					duration,
					timedOut
				)

			override def executedAtTimeOut(solutionsNo: Int): Unit =
				timedOut = true

			override def setChildrenListeners(timeOutListeners: Array[TimeOutListener]): Unit = Unit

			override def setChildrenListeners(timeOutListener: TimeOutListener): Unit = Unit
		}

		search.setSolutionListener(placementStatistics)
		search.setTimeOutListener(placementStatistics)
		search.setTimeOut(TIMEOUT)
		val select: SelectChoicePoint[IntVar] =
			new InputOrderSelect[IntVar](store, operatorHost,
				new IndomainMax[IntVar]())

		val result: Boolean = search.labeling(store, select, cost)


		val endTime = System.nanoTime()
		placementStatistics.generate((endTime - startTime) / 1000000)

		Predef.println("Time: " + (endTime - startTime))
		Predef.println("Store >>>\n" + store + "\n<<< Store")
		Predef.println(PlacementStatistics(placementId))

		if (result) {
			Predef.println("Solution:")
			for (op <- operatorHost)
				Predef.println(op)
			operatorHost.map(op => op.value())
		} else {
			Predef.println("*** No")
			null
		}
	}

	/**
	  * Adds remote operators to operator tree at operator links between operators, which are not placed on the same
	  * host
	  *
	  * @param query
	  * @param env
	  * @param placement
	  * @tparam Domain
	  * @return
	  */
	private def addRemotes[Domain: Manifest](
												query: IR.Rep[IR.Query[Domain]]
											)(
												implicit env: QueryEnvironment, placement: Map[IR.Rep[IR.Query[_]], Host]
											): IR.Rep[IR.Query[Domain]] = {

		val mDom = implicitly[Manifest[Domain]]

		import IR._

		def distributeUnary[TA: Manifest, T: Manifest](child: Rep[Query[TA]], build: Rep[Query[TA]] => Rep[Query[T]]): Rep[Query[T]] = {
			val host = placement(query)
			if (host != placement(child))
				build(remote(addRemotes(child), host))
			else
				build(addRemotes(child))
		}

		def distributeBinary[TA: Manifest, TB: Manifest, T: Manifest](c1: Rep[Query[TA]], c2: Rep[Query[TB]], build: (Rep[Query[TA]], Rep[Query[TB]]) => Rep[Query[T]]): Rep[Query[T]] = {
			val host = placement(query)
			val h1 = placement(c1)
			val h2 = placement(c2)

			if (host == h1 && host == h2)
				build(addRemotes(c1), addRemotes(c2))
			else if (host == h1 && host != h2)
				build(addRemotes(c1), remote(addRemotes(c2), host))
			else if (host != h1 && host == h2)
				build(remote(addRemotes(c1), host), addRemotes(c2))
			else
				build(remote(addRemotes(c1), host), remote(addRemotes(c2), host))
		}

		query match {
			//Base
			case QueryTable(_, _, _, _) => query
			case QueryRelation(_, _, _, _) => query
			case Def(Root(r, h)) => distributeUnary(r, (q: Rep[Query[Domain]]) => root(q, h))
			case Def(Materialize(r)) => distributeUnary(r, (q: Rep[Query[Domain]]) => materialize(q))

			//Basic Operators
			case Def(Selection(r, f)) => distributeUnary(r, (q: Rep[Query[Domain]]) => selection(q, f)(mDom, env))
			case Def(Projection(r, f)) => distributeUnary(r, (q: Rep[Query[Any]]) => projection(q, f))
			case Def(CrossProduct(r1, r2)) => distributeBinary(r1, r2, (q1: Rep[Query[Any]], q2: Rep[Query[Any]]) => crossProduct(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def(EquiJoin(r1, r2, eqs)) => distributeBinary(r1, r2, (q1: Rep[Query[Any]], q2: Rep[Query[Any]]) => equiJoin(q1, q2, eqs)).asInstanceOf[Rep[Query[Domain]]]
			case Def(DuplicateElimination(r)) => distributeUnary(r, (q: Rep[Query[Domain]]) => duplicateElimination(q))
			case Def(Unnest(r, f)) => distributeUnary(r, (q: Rep[Query[Any]]) => unnest(q, f)).asInstanceOf[Rep[Query[Domain]]]

			//Set theory operators
			case Def(UnionAdd(r1, r2)) => distributeBinary(r1, r2, (q1: Rep[Query[Any]], q2: Rep[Query[Any]]) => unionAdd(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def(UnionMax(r1, r2)) => distributeBinary(r1, r2, (q1: Rep[Query[Any]], q2: Rep[Query[Any]]) => unionMax(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def(Intersection(r1, r2)) => distributeBinary(r1, r2, (q1: Rep[Query[Any]], q2: Rep[Query[Any]]) => intersection(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def(Difference(r1, r2)) => distributeBinary(r1, r2, (q1: Rep[Query[Any]], q2: Rep[Query[Any]]) => difference(q1, q2)).asInstanceOf[Rep[Query[Domain]]]

			//Aggregation operators
			case Def(AggregationSelfMaintained(r, gr, start, fa, fr, fu, ck, conv)) =>
				distributeUnary(r, (q: Rep[Query[Any]]) => aggregationSelfMaintained(q, gr, start, fa, fr, fu, ck, conv))
			case Def(AggregationNotSelfMaintained(r, gr, start, fa, fr, fu, ck, conv)) =>
				distributeUnary(r, (q: Rep[Query[Any]]) => aggregationNotSelfMaintained(q, gr, start, fa, fr, fu, ck, conv))

			//Remote
			case Def(Reclassification(r, t)) => distributeUnary(r, (q: Rep[Query[Domain]]) => reclassification(q, t))
			case Def(Declassification(r, t)) => distributeUnary(r, (q: Rep[Query[Domain]]) => declassification(q, t))
			case Def(ActorDef(_, _, _)) => query

		}
	}

}
