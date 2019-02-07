package idb.algebra.remote.placement

import idb.algebra.QueryTransformerAdapter
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.exceptions.NoServerAvailableException
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.RemoteUtils
import idb.query.{Host, QueryEnvironment}

/**
  * CSPPlacementTransformer4
  */
trait CSPPlacementTransformer4
	extends QueryTransformerAdapter with QueryTaint {
	//Defines whether the query tree should be use fragments bigger then single operators
	val USE_PRIVACY: Boolean = true
	// If true, total cost is network cost * load coast, otherwise it is independent sum with network priority
	val TOTAL_COST_PRODUCT: Boolean = true

	val IR: RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RemoteUtils


	import IR._

	var nextOperatorId = 0

	case class Operator(
						   query: IR.Rep[IR.Query[_]],
						   pinnedTo: Option[Int],
						   children: Seq[Operator]
					   ) {
		val id: Int = nextOperatorId
		nextOperatorId += 1

		lazy val selectivity: Float =
			if (selectivityLib != null) {
				selectivityLib(query.hashCode)
			}
			else
				query match {
					//Base
					case QueryTable(_, _, _, _) => 1f
					case QueryRelation(_, _, _, _) => 1f
					case Def(Root(_, _, _)) => 1f
					case Def(Materialize(_)) => 1f

					//Basic Operators
					case Def(Selection(_, _)) => 0.5f
					case Def(Projection(_, _)) => 1f
					case Def(CrossProduct(_, _)) =>
						val c1 = children.head
						val c2 = children(1)
						(c1.outgoingLink * c2.outgoingLink) / (c1.outgoingLink + c2.outgoingLink)
					case Def(EquiJoin(_, _, _)) =>
						val c1 = children.head
						val c2 = children(1)
						2 * Math.min(c1.outgoingLink, c2.outgoingLink) / (c1.outgoingLink + c2.outgoingLink)
					case Def(DuplicateElimination(_)) => 0.5f
					case Def(Unnest(_, _)) => 5f

					//Set theory operators
					case Def(UnionAdd(_, _)) => 1f
					case Def(UnionMax(_, _)) => 1f
					case Def(Intersection(_, _)) => 0.5f
					case Def(Difference(_, _)) => 0.5f

					//Aggregation operators
					case Def(AggregationSelfMaintained(_, _, _, _, _, _, _, _)) => 2f
					case Def(AggregationNotSelfMaintained(_, _, _, _, _, _, _, _)) => 2f

					//Remote
					case Def(Reclassification(_, _)) => 1f
					case Def(Declassification(_, _)) => 1f
					case Def(ActorDef(_, _, _)) => 1f
				}

		lazy val load: Float =
			query match {
				//Base
				case QueryTable(_, _, _, _) => 0f
				case QueryRelation(_, _, _, _) => 0f
				case Def(Root(_, _, _)) => 0f
				case Def(Materialize(_)) => 2f

				//Basic Operators
				case Def(Selection(_, _)) => 1f
				case Def(Projection(_, _)) => 1f
				case Def(CrossProduct(_, _)) => 8f
				case Def(EquiJoin(_, _, _)) => 4f
				case Def(DuplicateElimination(_)) => 2f
				case Def(Unnest(_, _)) => 1f

				//Set theory operators
				case Def(UnionAdd(_, _)) => 1f
				case Def(UnionMax(_, _)) => 4f
				case Def(Intersection(_, _)) => 4f
				case Def(Difference(_, _)) => 4f

				//Aggregation operators
				case Def(AggregationSelfMaintained(_, _, _, _, _, _, _, _)) => 3f
				case Def(AggregationNotSelfMaintained(_, _, _, _, _, _, _, _)) => 3f

				//Remote
				case Def(Reclassification(_, _)) => 0f
				case Def(Declassification(_, _)) => 0f
				case Def(ActorDef(_, _, _)) => 0f
			}

		lazy val outgoingLink: Float =
			if (children.isEmpty) selectivity * 1000
			else children.map(_.outgoingLink).sum * selectivity

		lazy val toList: Seq[Operator] =
			Seq(this) ++ children.flatMap(child => child.toList)

		override def toString: String = s"($id, query: ${query.hashCode}, load $load, selectivity $selectivity, pinnedTo $pinnedTo, $query, children ${children.map(_.id)})\n"
	}

	case class Link(
					   sender: Int,
					   receiver: Int,
					   load: Float
				   ) {
		override def toString: String = s"($sender => $receiver: $load)\n"
	}

	/**
	  * Maps the hashcode of an operator to it's selectivity. If 0, default values are used. If not null and an
	  * operator is initialized that is not mentioned in the Map, a NoSuchElementException is thrown
	  */
	var selectivityLib: Map[Int, Float] = _

	override def transform[Domain: Manifest](relation: IR.Rep[IR.Query[Domain]])(implicit env: QueryEnvironment): IR.Rep[IR.Query[Domain]] = {

		println("REL" + relation.hashCode())

		//		println("global Defs = ")
		//		IR.globalDefsCache.toList.sortBy(t => t._1.id).foreach(println)

		// Init selectivity lib
		selectivityLib =
			if (SelectivityLib.libs.contains(relation.hashCode())) {
				println(s"Using predefined selectivity lib for query ${relation.hashCode()}")
				SelectivityLib.libs(relation.hashCode())
			}
			else {
				println(s"Using default selectivity values for query ${relation.hashCode()}")
				null
			}

		val hostList = env.hosts.toSeq
		val hostId = hostList.zipWithIndex.toMap
		val hostCapacity = hostList.map(env.priorityOf)
		//Prepare data for CSP Solver function
		val operatorTree: Operator = operatorTreeFrom(relation, hostId)
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
		val placement: Seq[Int] = computePlacement(operators, operatorHosts, links, hostCapacity)

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

	private def operatorTreeFrom(query: IR.Rep[IR.Query[_]], hostId: Map[Host, Int]): Operator = {

		query match {
			//Base
			case QueryTable(_, _, _, h) =>
				Operator(query, Some(hostId(h)), Seq.empty)
			case QueryRelation(_, _, _, h) =>
				Operator(query, Some(hostId(h)), Seq.empty)
			case Def(Root(r, h, _)) =>
				Operator(query, Some(hostId(h)), Seq(operatorTreeFrom(r, hostId)))
			case Def(Materialize(r)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))

			//Basic Operators
			case Def(Selection(r, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))
			case Def(Projection(r, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))
			case Def(CrossProduct(r1, r2)) =>
				val c1 = operatorTreeFrom(r1, hostId)
				val c2 = operatorTreeFrom(r2, hostId)
				Operator(query, None, Seq(c1, c2))
			case Def(EquiJoin(r1, r2, _)) =>
				val c1 = operatorTreeFrom(r1, hostId)
				val c2 = operatorTreeFrom(r2, hostId)
				Operator(query, None, Seq(c1, c2))
			case Def(DuplicateElimination(r)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))
			case Def(Unnest(r, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))

			//Set theory operators
			case Def(UnionAdd(r1, r2)) =>
				val c1 = operatorTreeFrom(r1, hostId)
				val c2 = operatorTreeFrom(r2, hostId)
				Operator(query, None, Seq(c1, c2))
			case Def(UnionMax(r1, r2)) =>
				val c1 = operatorTreeFrom(r1, hostId)
				val c2 = operatorTreeFrom(r2, hostId)
				Operator(query, None, Seq(c1, c2))
			case Def(Intersection(r1, r2)) =>
				val c1 = operatorTreeFrom(r1, hostId)
				val c2 = operatorTreeFrom(r2, hostId)
				Operator(query, None, Seq(c1, c2))
			case Def(Difference(r1, r2)) =>
				val c1 = operatorTreeFrom(r1, hostId)
				val c2 = operatorTreeFrom(r2, hostId)
				Operator(query, None, Seq(c1, c2))

			//Aggregation operators
			case Def(AggregationSelfMaintained(r, _, _, _, _, _, _, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))
			case Def(AggregationNotSelfMaintained(r, _, _, _, _, _, _, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))

			//Remote
			case Def(Reclassification(r, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))
			case Def(Declassification(r, _)) =>
				Operator(query, None, Seq(operatorTreeFrom(r, hostId)))
			case Def(ActorDef(_, h, _)) =>
				Operator(query, Some(hostId(h)), Seq.empty)
		}
	}

	private def computePlacement(
									operators: Seq[Operator],
									operatorHosts: Seq[Seq[Int]],
									links: Seq[Link],
									hostCapacity: Seq[Int]
								): Seq[Int] = {
		import org.jacop.constraints._
		import org.jacop.constraints.binpacking.Binpacking
		import org.jacop.core._
		import org.jacop.floats.constraints._
		import org.jacop.floats.core._
		import org.jacop.search._

		println("Input: ")
		println("operators: " + operators)
		println("operator hosts: " + operatorHosts)
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
		val operatorLoad = new Array[Int](numOperators)
		operators.zipWithIndex foreach { t =>
			val (operator, operatorId) = t
			operatorHost(operatorId) = new IntVar(store, s"operator${operatorId}host", 0, numHosts - 1)
			operatorLoad(operatorId) = operator.load.toInt
		}

		// Add host placement constraints
		operators.zipWithIndex.foreach { t =>
			val (operator, operatorId) = t

			// Pin operator on host if needed
			operator.pinnedTo match {
				case Some(hostId) => store.impose(new XeqC(operatorHost(operatorId), hostId))
				case None =>
			}

			// Add invalid operator on host placement constraints
			(0 until numHosts) foreach { hostId =>
				if (!operatorHosts(operatorId).contains(hostId))
					store.impose(new XneqC(operatorHost(operatorId), hostId))
			}
		}

		val operatorLoadSum = operatorLoad.sum
		val hostCapacitySum = hostCapacity.sum
		val maxCapacitySqr = Math.pow(hostCapacitySum, 2).toInt

		// Load on each host  (sum of operator loads on the host)
		val hostLoad = new Array[IntVar](numHosts)
		val hostLoadFloat = new Array[FloatVar](numHosts)
		// hostLoad * (capacity sum / (load sum * host capacity))
		val hostLoadQuotient = new Array[FloatVar](numHosts)
		// sqr(hostLoadQuotient)s
		val hostLoadCostFloat = new Array[FloatVar](numHosts)
		// Reduce variance in conversion (influence of range) by multiplication with 1000
		val hostLoadCost1000 = new Array[IntVar](numHosts)
		val hostLoadCostFloat1000 = new Array[FloatVar](numHosts)
		(0 until numHosts) foreach { host =>
			hostLoad(host) = new IntVar(store, s"host${host}load", 0, operatorLoadSum)
			hostLoadFloat(host) = new FloatVar(store, s"host${host}load", 0, operatorLoadSum)
			store.impose(new XeqP(hostLoad(host), hostLoadFloat(host)))

			hostLoadQuotient(host) = new FloatVar(store, s"host${host}loadQuotient", 0, hostCapacitySum)
			store.impose(new PmulCeqR(hostLoadFloat(host), hostCapacitySum.toDouble / (operatorLoadSum * hostCapacity(host)), hostLoadQuotient(host)))

			hostLoadCostFloat(host) = new FloatVar(store, s"host${host}loadCost" + host, 0, maxCapacitySqr)
			store.impose(new PmulQeqR(hostLoadQuotient(host), hostLoadQuotient(host), hostLoadCostFloat(host)))

			hostLoadCost1000(host) = new IntVar(store, s"host${host}loadCost1000" + host, 0, maxCapacitySqr * 1000)
			hostLoadCostFloat1000(host) = new FloatVar(store, s"host${host}loadCost" + host, 0, maxCapacitySqr * 1000)
			store.impose(new XeqP(hostLoadCost1000(host), hostLoadCostFloat1000(host)))
			store.impose(new PmulCeqR(hostLoadCostFloat(host), 1000, hostLoadCostFloat1000(host)))
		}
		//Define bin packing constraint (= load on all servers)
		// host load(i) = sum_j(operatorLoad(j) | operatorHost(j) == i)
		store.impose(new Binpacking(operatorHost, hostLoad, operatorLoad))


		val loadSum = new IntVar(store, "load-sum", 0, maxCapacitySqr * numHosts * 1000)
		val loadSumFloat = new FloatVar(store, "load-sum", 0, maxCapacitySqr * numHosts * 1000)
		store.impose(new XeqP(loadSum, loadSumFloat))
		store.impose(new SumInt(store, hostLoadCost1000, "==", loadSum))

		// Load on each link
		val linkLoad = new Array[IntVar](numLinks)
		val maxBandwidth = links.map(_.load.toInt).sum
		(0 until numLinks) foreach { linkId =>
			linkLoad(linkId) = new IntVar(store, "link" + linkId, 0, maxBandwidth)
		}
		links.zipWithIndex foreach { t =>
			val (link, linkId) = t
			store.impose(
				new IfThenElse(
					new XeqY(
						operatorHost(link.sender),
						operatorHost(link.receiver)),
					new XeqC(linkLoad(linkId), 0),
					new XeqC(linkLoad(linkId), link.load.toInt)
				)
			)
		}

		val cost =
			if (TOTAL_COST_PRODUCT) {
				//Define network cost
				val networkCost = new IntVar(store, "network-sum", 0, maxBandwidth)
				store.impose(new SumInt(store, linkLoad, "==", networkCost))

				// cost = load * network
				val loadSum2 = new IntVar(store, "load-sum-small", 0, maxCapacitySqr * numHosts)
				val thousand = new IntVar(store, "1000", 1000, 1000)
				store.impose(new XeqC(thousand, 1000))
				store.impose(new XdivYeqZ(loadSum, thousand, loadSum2)) // Divide by thousand to avoid overflow
				val cost = new IntVar(store, "cost", 0, maxBandwidth * maxCapacitySqr * numHosts)
				store.impose(new XmulYeqZ(loadSum2, networkCost, cost))

				cost
			}
			else {
				//Define network cost
				val networkSum = new IntVar(store, "network-sum", 0, maxBandwidth)
				store.impose(new SumInt(store, linkLoad, "==", networkSum))
				val networkCost = new IntVar(store, "network-cost", 0, maxBandwidth * 1000)
				store.impose(new XmulCeqZ(networkSum, 1000, networkCost))

				// Load Cost
				val loadCost = new IntVar(store, "load-cost", 0, 1000)
				val loadCostFloat = new FloatVar(store, "load-cost", 0, 1000)
				store.impose(new XeqP(loadCost, loadCostFloat))
				store.impose(new PmulCeqR(loadSumFloat, 1000D / (maxCapacitySqr * 1000 * numHosts), loadCostFloat))


				// cost = load * network
				val cost = new IntVar(store, "cost", 0, maxBandwidth * 1000)
				store.impose(new XplusYeqZ(loadCost, networkCost, cost))

				cost
			}

		// Search for a solution (Minimize cost) and print results
		val search: Search[IntVar] = new DepthFirstSearch[IntVar]()
		val select: SelectChoicePoint[IntVar] =
			new InputOrderSelect[IntVar](store, operatorHost,
				new IndomainMin[IntVar]())
		val result: Boolean = search.labeling(store, select, cost)


		val endTime = System.nanoTime()

		Predef.println("Time: " + (endTime - startTime))
		Predef.println("Store >>>\n" + store + "\n<<< Store")

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
			case Def(Root(r, h, placementId)) => distributeUnary(r, (q: Rep[Query[Domain]]) => root(q, h, placementId))
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
