package ivm.optimization

import ivm.expressiontree._
import Lifting._
import Util._
import collection.generic.FilterMonadic
import scala.collection.Map

// Contract: Each map entry has the form Exp[T] -> T for some T
class SubquerySharing(val subqueries: Map[Exp[_],Any]) {
   val directsubqueryShare: Exp[_] => Exp[_] = {
      (e) => subqueries.get(Optimization.normalize(e)) match {
        case Some(t) => Const(t)
        case None => e
      }
    }

   private def groupByShareBody[T, T2](c: Exp[Traversable[T]],
                                                        f: FuncExp[T, Boolean],
                                                        fEqBody: Eq[T2], lhs: Exp[T2], rhs: Exp[T2]) = {
     /*
     //How the fuck does this typecheck?
     val groupedBy2: Exp[T => Traversable[T]] = c.map(identity).groupBy(FuncExp.makefun[T, T2](fEqBody.y, f.x).f)
     //Reason: a FuncExp[T, T2] is also FuncExp[T, Any]; groupBy(f: FuncExp[T, Any]) gives Map[Any, Repr]
     // (where Repr = Traversable[T] here) which can be _upcast_ to Any => Repr and then further upcast to T => Repr.
     // It's an interesting anomaly of type inference with variance, but it does not produce unsoundness.
     // Indeed, since Map[A, +B] is invariant in A, we can't upcast Map[Any, Repr] to Map[T, Repr] - see groupedBy5.
     //val groupedBy3: Exp[T => Traversable[T]] = c.map(identity).groupBy[T2](FuncExp.makefun[T, T2](fEqBody.y, f.x).f)
     val groupedBy4: Exp[T => Traversable[T]] = c.map(identity).groupBy[Any](FuncExp.makefun[T, T2](fEqBody.y, f.x).f)
     //
     val groupedBy5: Exp[Map[Any, Traversable[T]]] = c.map(identity).groupBy[Any](FuncExp.makefun[T, T2](fEqBody.y, f.x).f)
     */
     //This code:
     //val groupedBy = c.map(identity).groupBy(FuncExp.makefun[T, T2](fEqBody.y, f.x).f)
     //expands to this:

     val groupedBy = c.groupBy[T2](FuncExp.makefun[T, T2](rhs, f.x).f)

     assertType[Exp[T2 => Traversable[T]]](groupedBy) //Just for documentation.
     subqueries.get(Optimization.normalize(groupedBy)) match {
       case Some(t) => Some(App(Const(t.asInstanceOf[T2 => Traversable[T]]), lhs))
       case None => None
     }
   }

  private def residualQuery[T](e: Exp[Traversable[T]], conds: Set[Exp[Boolean]], v: TypedVar[_ /*T*/]) : Exp[FilterMonadic[T, Traversable[T]]] = {
    if (conds.isEmpty) return e
    val residualcond : Exp[Boolean] = conds.reduce( (x,y) => And(x,y))
    e.withFilter(FuncExp.makefun[T,Boolean](residualcond,v))

  }
  private def tryGroupBy[T](c: Exp[Traversable[T]],
                               allConds: Set[Exp[Boolean]],
                               f: FuncExp[T,Boolean])
                               (equ : Exp[Boolean])
                                  : Option[Exp[FilterMonadic[T, Traversable[T]]]] = {
    equ match {
      case eq : Eq[t2] => {
       val oq : Option[Exp[Traversable[T]]] =
        if (eq.x.isOrContains(f.x) && !eq.y.isOrContains(f.x))
                 groupByShareBody[T,t2](c, f, eq, eq.y, eq.x)
              else if (eq.y.isOrContains(f.x) && !eq.x.isOrContains(f.x))
                 groupByShareBody[T,t2](c, f, eq, eq.x, eq.y)
              else None
       oq.map( (e) => residualQuery(e, allConds-eq, f.x)) }
      case _ => None
    }
  }

  //XXX: we should strip View if needed on _both_ sides before performing the match.
  val groupByShare2: Exp[_] => Exp[_] = {
   e => e match {
       case Filter(c: Exp[Traversable[_ /*t*/]], f: FuncExp[t, _/*Boolean*/]) =>
         val conds : Set[Exp[Boolean]] = BooleanOperators.cnf(f.body)
         //Function.unlift is expensive.
         val optimized : Option[Exp[_]]=
            conds.collectFirst( Function.unlift( tryGroupBy(Optimization.stripView(c.asInstanceOf[Exp[Traversable[t]]]),conds,f)))
         optimized.getOrElse(e)
       case _ => e
   }
 }

   val groupByShare: Exp[_] => Exp[_] = {
    e => e match {
        case Filter(View(c: Exp[Traversable[_ /*t*/]]), (f: FuncExp[t, _/*Boolean*/]) & FuncExpBody(fEqBody: Eq[t2])) =>
          val Eq(lhs, rhs) = fEqBody
          val coll = Optimization.stripView(c.asInstanceOf[Exp[Traversable[t]]])
          if (rhs.isOrContains(f.x) && !lhs.isOrContains(f.x))
            groupByShareBody[t, t2](coll, f, fEqBody, lhs, rhs).getOrElse(e)
          else if (lhs.isOrContains(f.x) && !rhs.isOrContains(f.x))
            groupByShareBody[t, t2](coll, f, fEqBody, rhs, lhs).getOrElse(e)
          else
            e
        case _ => e
    }
  }

  def shareSubqueries[T](query: Exp[T]) : Exp[T] = {
      query.transform(directsubqueryShare.andThen(groupByShare2))
  }
}