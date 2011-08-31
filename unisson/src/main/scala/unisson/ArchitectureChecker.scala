package unisson

import ast._
import sae.LazyView
import sae.collections.{QueryResult, Conversions}
import sae.bytecode.BytecodeDatabase
import sae.syntax.RelationalAlgebraSyntax

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:42
 *
 */

class ArchitectureChecker(val db: BytecodeDatabase)
{

    private var ensembles: Map[Ensemble, QueryResult[SourceElement[AnyRef]]] = Map()

    private var constraintViolations: Map[DependencyConstraint, QueryResult[Violation]] = Map()

    private var allViolations = new QueryResult[Violation]
    {
        def lazyInitialize {}
        protected def materialized_foreach[T](f : (Violation) => T) {}
        protected def materialized_size : Int = 0
        protected def materialized_singletonValue : Option[Violation] = None
        protected def materialized_contains(v : Violation) : Boolean = false
    }

    def getEnsembles = ensembles.keySet

    def ensembleStatistic(ensemble : Ensemble) =

        ensemble.name + ":\n" +
        "   " + "#elements: " + ensembleElements(ensemble).size + "\n" +
        "   " + "constraints:\n" +
        (ensemble.outgoingConnections.map( _ match
            {
                case OutgoingConstraint(_,targets,kind) => "   " + "outgoing    " + kind + " to " + targets +"\n"
                case NotAllowedConstraint(_,_,_,target,_,kinds) => "   " + "not_allowed " + kinds +  " to " + target +"\n"
                case ExpectedConstraint(_,_,_,target,_,kinds) => "   " + "expected    " + kinds +  " to " + target +"\n"
                case _ => ""
            }
        ).foldLeft("")(_ + _)) +
        (ensemble.incomingConnections.map( _ match
            {
                case IncomingConstraint(sources,_,kind) => "   " + "incoming    " + kind + " from " + sources +"\n"
                case NotAllowedConstraint(_,source,_,_,_,kinds) => "   " + "not_allowed " + kinds +  " from " + source +"\n"
                case ExpectedConstraint(_,source,_,_,_,kinds) => "   " + "expected    " + kinds +  " from " + source +"\n"
                case _ => ""
            }
        ).foldLeft("")(_ + _))


    def addEnsemble(ensemble: Ensemble, query: LazyView[SourceElement[AnyRef]])
    {
        ensembles += {
            ensemble -> Conversions.lazyViewToResult(query)
        }
    }

    def hasEnsemble(ensemble: Ensemble) = ensembles.isDefinedAt(ensemble)

    // TODO in the long run, make this return an iterable
    def ensembleElements(ensemble: Ensemble): QueryResult[SourceElement[AnyRef]] =
    {
        ensembles.get(ensemble).get
    }

    def addConstraint(constraint: DependencyConstraint, query: LazyView[Violation])
    {
        import RelationalAlgebraSyntax._

        constraintViolations += {
            constraint -> Conversions.lazyViewToResult(query)
        }
        allViolations = allViolations ∪ query // union should yield all elements in foreach, as it just delegates to the underlying views, which are materialized
    }


    def hasConstraint(constraint: DependencyConstraint) = constraintViolations.isDefinedAt(constraint)

    def violations(constraint: DependencyConstraint): QueryResult[Violation] = constraintViolations(
        constraint
    )

    def violations: QueryResult[Violation] =
    {
        allViolations
    }

}