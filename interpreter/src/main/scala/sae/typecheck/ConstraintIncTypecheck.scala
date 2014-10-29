package sae.typecheck

import idb.SetTable
import idb.syntax.iql._
import idb.syntax.iql.IR._

import sae.typecheck.Exp._
import sae.typecheck.Type._
import sae.typecheck.Constraint._

/**
* Created by seba on 26/10/14.
*/
object ConstraintIncTypecheck {

  case object Num extends ExpKind
  case object Add extends ExpKind
  case object Var extends ExpKind
  case object Abs extends ExpKind
  case object App extends ExpKind

  case object TNum extends Type {
    def rename(ren: Map[Symbol, Symbol]) = this
    def subst(s: TSubst) = this
    def unify(other: Type): Option[TSubst] = other match {
      case TNum => scala.Some(Map())
      case TVar(x) => scala.Some(Map(x -> this))
      case _ => None
    }
  }
  case class TVar(x: Symbol) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TVar(ren.getOrElse(x, x))
    def subst(s: Map[Symbol, Type]) = s.getOrElse(x, this)
    def unify(other: Type): Option[TSubst] = scala.Some(Map(x -> other))
  }
  case class TFun(t1: Type, t2: Type) extends Type {
    def rename(ren: Map[Symbol, Symbol]) = TFun(t1.rename(ren), t2.rename(ren))
    def subst(s: Map[Symbol, Type]) = TFun(t1.subst(s), t2.subst(s))
    def unify(other: Type): Option[TSubst] = other match {
      case TFun(t1_, t2_) =>
        t1.unify(t1_) match {
          case scala.None => None
          case scala.Some(s1) => t2.subst(s1).unify(t2_.subst(s1)) match {
            case scala.None => None
            case scala.Some(s2) => scala.Some(s1.mapValues(_.subst(s2)) ++ s2)
          }
        }
      case TVar(x) => scala.Some(Map(x -> this))
      case _ => None
    }
  }

  case class EqConstraint(expected: Type, actual: Type) extends Constraint {
    def rename(ren: Map[Symbol, Symbol]) = EqConstraint(expected.rename(ren), actual.rename(ren))
  }

  case class VarRequirement(x: Symbol, t: Type) extends Requirement {
    def merge(r: Requirement) = r match {
      case VarRequirement(`x`, t2) => scala.Some((scala.Seq(EqConstraint(t, t2)), scala.Seq(this)))
      case _ => None
    }
    def rename(ren: Map[Symbol, Symbol]) = VarRequirement(ren.getOrElse(x, x), t.rename(ren))
  }

  def freshStepRep: Rep[((ExpKind, Seq[Lit], Seq[FreshData])) => FreshData] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[FreshData])) => freshStep(p._1, p._2, p._3)
  )

  def freshStep(e: ExpKind, lits: Seq[Lit], sub: Seq[FreshData]): FreshData = {
    import scala.collection.Seq
    e match {
      case Num => (Seq(), Seq(Map()))
      case Add =>
        val (mfree, ren) = mergeFree(sub(0)._1, sub(1)._1)
        (mfree, Seq(ren))
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        (Seq(Symbol("X_" + x.name)), Seq(Map()))
      case App =>
        val (mfree, ren) = mergeFree(sub(1)._1, sub(2)._1)
        val x = tick('X$App, mfree)
        (x +: mfree, Seq(ren))
      case Abs =>
        val x = tick('X$abs, sub(0)._1)
        (x +: sub(0)._1, Seq(Map()))
      case Root.Root => if (sub.isEmpty) (Seq(), Seq(Map())) else sub(0)
    }
  }

  def typecheckStepRep: Rep[((ExpKind, Seq[Lit], Seq[ConstraintIncData], FreshData)) => ConstraintIncData] = staticData (
    (p: (ExpKind, Seq[Lit], Seq[ConstraintIncData], FreshData)) => typecheckStep(p._1, p._2, p._3, p._4)
  )

  def typecheckStep(e: ExpKind, lits: Seq[Lit], sub: Seq[ConstraintIncData], fresh: FreshData): ConstraintIncData = {
    import scala.collection._
    import Predef.Set
    e match {
      case Num => (TNum, Seq(), Seq())
      case Add =>
        val (t1, cons1, reqs1) = sub(0)
        val (_t2, _cons2, _reqs2) = sub(1)
        val Seq(ren) = fresh._2
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val lcons = EqConstraint(TNum, t1)
        val rcons = EqConstraint(TNum, t2)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (TNum, lcons +: rcons +: (cons1 ++ cons2 ++ mcons), mreqs)
      case Var =>
        val x = lits(0).asInstanceOf[Symbol]
        val X = TVar(fresh._1(0))
        (X, Seq(), Seq(VarRequirement(x, X)))
      case App =>
        val (t1, cons1, reqs1) = sub(0)
        val (_t2, _cons2, _reqs2) = sub(1)
        val Seq(ren) = fresh._2
        val (t2, cons2, reqs2) = rename(ren)(_t2, _cons2, _reqs2)

        val X = TVar(fresh._1(0))
        val fcons = EqConstraint(TFun(t2, X), t1)
        val (mcons, mreqs) = mergeReqs(reqs1, reqs2)
        (X, fcons +: (cons1 ++ cons2 ++ mcons), mreqs)
      case Abs =>
        val x = lits(0).asInstanceOf[Symbol]
        val (t, cons, reqs) = sub(0)

        val X = TVar(fresh._1(0))
        val (xreqs, otherReqs) = reqs.partition{case VarRequirement(`x`, _) => true; case _ => false}
        val xcons = xreqs map {case VarRequirement(_, t) => EqConstraint(X, t)}
        (TFun(X, t), cons ++ xcons, otherReqs)
      case Root.Root =>
        if (sub.isEmpty)
          (TVar('Uninitialized), Seq(EqConstraint(TNum, TFun(TNum, TNum))), Seq())
        else {
          val (t, cons, reqs) = sub(0)
          (Root.TRoot(t), cons, reqs)
        }
    }
  }

//  type FreshFunTuple = (ExpKind, Seq[Lit] => Symbol)
//  val freshForKind = SetTable.empty[FreshFunTuple]
//  def initFreshForKind(): Unit = {
//    freshForKind += ((Var, (lits: Seq[Lit]) => {
//      val x = lits(0).asInstanceOf[Symbol]
//      Symbol("X_" + x.name)
//    }))
//    freshForKind += ((App, lits => Symbol("X$App")))
//    freshForKind += ((Abs, lits => Symbol("X$Abs")))
//  }
//
//  val fresh = WITH.RECURSIVE[FreshTuple] (fresh =>
//      (SELECT ((e: Rep[ExpTuple], f: Rep[FreshFunTuple]) => id(e) -> f._2(lits(e)))
//       FROM (Exp.table, freshForKind)
//       WHERE ((e, f) => kind(e) == f._1))
//    UNION ALL (
//      (SELECT ((e: Rep[ExpTuple], v: Rep[FreshTuple]) => id(e) -> v._2)
//       FROM (Exp.table, fresh) // propagate >=1-ary
//       WHERE ((e,v) => subseq(e).length >= 1 AND subseq(e)(0) == v._1))
//    UNION ALL (
//      (SELECT ((e: Rep[ExpTuple], v: Rep[FreshTuple]) => id(e) -> v._2)
//       FROM (Exp.table, fresh) // propagate >=2-ary
//       WHERE ((e,v) => subseq(e).length >= 2 AND subseq(e)(1) == v._1)))
//    )
//  )

  val fresh = WITH.RECURSIVE[FreshTuple] (fresh =>
      (SELECT ((e: Rep[ExpTuple]) => id(e) -> freshStepRep ((kind(e), lits(e), Seq())))
       FROM Exp.table // 0-ary
       WHERE (e => subseq(e).length == 0))
    UNION ALL (
      (SELECT ((e: Rep[ExpTuple], f1: Rep[FreshTuple]) => id(e) -> freshStepRep ((kind(e), lits(e), Seq(f1._2))))
       FROM (Exp.table, fresh) // 1-ary
       WHERE ((e,f1) => subseq(e).length == 1
                    AND subseq(e)(0) == f1._1))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], f1: Rep[FreshTuple], f2: Rep[FreshTuple]) => id(e) -> freshStepRep ((kind(e), lits(e), Seq(f1._2, f2._2))))
       FROM (Exp.table, fresh, fresh) // 2-ary
       WHERE ((e,f1,f2) => subseq(e).length == 2
                       AND subseq(e)(0) == f1._1
                       AND subseq(e)(1) == f2._1))
    )
  )


  val constraints = WITH.RECURSIVE[ConstraintIncTuple] (constraints =>
      (SELECT ((e: Rep[ExpTuple], f: Rep[FreshTuple]) => id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(), f._2)))
       FROM (Exp.table, fresh) // 0-ary
       WHERE ((e,f) => id(e) == f._1
                   AND subseq(e).length == 0))
    UNION ALL ( queryToInfixOps
      (SELECT ((e: Rep[ExpTuple], f: Rep[FreshTuple], t1: Rep[ConstraintIncTuple]) =>
         id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(t1._2), f._2)))
       FROM (Exp.table, fresh, constraints) // 1-ary
       WHERE ((e,f,t1) => id(e) == f._1
                      AND subseq(e).length == 1
                      AND subseq(e)(0) == t1._1))
    UNION ALL
      (SELECT ((e: Rep[ExpTuple], f: Rep[FreshTuple], t1: Rep[ConstraintIncTuple], t2: Rep[ConstraintIncTuple]) =>
         id(e) -> typecheckStepRep ((kind(e), lits(e), Seq(t1._2, t2._2), f._2)))
       FROM (Exp.table, fresh, constraints, constraints) // 2-ary
       WHERE ((e,f,t1,t2) => id(e) == f._1
                         AND subseq(e).length == 2
                         AND subseq(e)(0) == t1._1
                         AND subseq(e)(1) == t2._1))
    )
  )

  def solveConstraints(cons: Seq[Constraint]): (TSubst, Seq[Constraint]) = {
    var unres = scala.Seq[Constraint]()
    var res = Map[Symbol, Type]()
    for (c <- cons) c match {
      case EqConstraint(t1, t2) => t1.subst(res).unify(t2.subst(res)) match {
        case scala.Some(s) => res = res.mapValues(_.subst(s)) ++ s
        case scala.None => unres = cons +: unres
      }
    }
    (res, unres)
  }


  val rootTypeExtractor: ConstraintIncData => Either[Type, TError] = (x: ConstraintIncData) => {
    val (t, cons, reqs) = x
    if (!reqs.isEmpty)
      scala.Right(s"Unresolved context requirements $reqs, type $t, constraints $cons")
    else {
      val (s, unres) = solveConstraints(cons)
      if (unres.isEmpty)
        t.subst(s) match {
          case Root.TRoot(t) => scala.Left(t)
          case _ => throw new RuntimeException(s"Unexpected root type $t")
        }
      else
        scala.Right(s"Unresolved constraints $unres, type ${t.subst(s)}")
    }
  }

  def main(args: Array[String]): Unit = {
    val resultTypes = constraints.asMaterialized
    val freshVars = fresh.asMaterialized
    val root = Root(constraints, staticData (rootTypeExtractor))

    val e = Add(Num(17), Add(Num(10), Num(2)))
    root.set(e)
    Predef.println(s"Type of $e is ${root.Type}")
    freshVars foreach (Predef.println(_))
    Predef.println()

    val e2 = Abs('x, Add(Num(10), Num(2)))
    root.set(e2)
    Predef.println(s"Type of $e2 is ${root.Type}")
    freshVars foreach (Predef.println(_))
    Predef.println()

    val e3 = Abs('x, Add(Num(10), Var('x)))
    root.set(e3)
    Predef.println(s"Type of $e3 is ${root.Type}")
    freshVars foreach (Predef.println(_))
    Predef.println()

    val e4 = Abs('x, Add(Var('x), Var('x)))
    root.set(e4)
    Predef.println(s"Type of $e4 is ${root.Type}")
    freshVars foreach (Predef.println(_))
    Predef.println()

    val e5 = Abs('x, Add(Var('err), Var('x)))
    root.set(e5)
    Predef.println(s"Type of $e5 is ${root.Type}")
    freshVars foreach (Predef.println(_))
    Predef.println()

    val e6 = Abs('x, Abs('y, Add(Var('x), Var('y))))
    root.set(e6)
    Predef.println(s"Type of $e6 is ${root.Type}")
    freshVars foreach (Predef.println(_))
    Predef.println()

//    val e6 = Var('x)
//    root.set(e6)
//    Predef.println(s"Type of $e6 is ${root.Type}")
//
//    val e7 = Abs(scala.Seq('y), scala.Seq(Var('y)))
//    root.set(e7)
//    Predef.println(s"Type of $e7 is ${root.Type}")
  }

}