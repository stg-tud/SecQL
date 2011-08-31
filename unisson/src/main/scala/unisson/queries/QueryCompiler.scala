package unisson.queries

import sae.LazyView
import sae.bytecode.BytecodeDatabase
import unisson.ast._
import unisson.{Violation, ArchitectureChecker, Queries, SourceElement}
import sae.bytecode.model.dependencies.{return_type, parameter, Dependency}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:17
 *
 */

class QueryCompiler(val checker : ArchitectureChecker)
{

    val queries = new Queries(checker.db)

    import queries._

    def addAll(definitions : Seq[UnissonDefinition])
    {
        definitions.map(add(_))
    }

    def add(definition : UnissonDefinition)
    {
        definition match {
            case e @ Ensemble(_,_,_) => existingQuery(e).getOrElse(createEnsembleQuery(e))
            case inc @ IncomingConstraint(_,_,_) => existingQuery(inc).getOrElse(createIncomingQuery(inc))
            case out @ OutgoingConstraint(_,_,_) => existingQuery(out).getOrElse(createOutgoingQuery(out))
            case not @ NotAllowedConstraint(_,_,_,_,_,_) => existingQuery(not).getOrElse(createNotAllowedQuery(not))
            case exp @ ExpectedConstraint(_,_,_,_,_,_) => existingQuery(exp).getOrElse(createExpectedQuery(exp))
        }
    }


    def createEnsembleQuery(ensemble:Ensemble): LazyView[SourceElement[AnyRef]] =
    {
        if( checker.hasEnsemble(ensemble) )
        {
            checker.ensembleElements(ensemble)
        }
        else
        {
            val query = compileUnissonQuery(ensemble.query)
            checker.addEnsemble(ensemble, query)
            query
        }
    }

    def compileUnissonQuery(query : UnissonQuery) : LazyView[SourceElement[AnyRef]] =
    {
        query match
        {
            case ClassQuery(pn, sn) => `class`(pn, sn)
            case ClassWithMembersQuery(ClassQuery(pn, sn)) => class_with_members(pn, sn)
            case ClassWithMembersQuery(classQuery) => class_with_members(compileUnissonQuery(classQuery))
            case PackageQuery(pn) => `package`(pn)
            case OrQuery(left, right) => compileUnissonQuery(left) or compileUnissonQuery(right)
            case WithoutQuery(left, right) => compileUnissonQuery(left) without compileUnissonQuery(right)
            case TransitiveQuery(innerQuery) => transitive(compileUnissonQuery(innerQuery))
            case SuperTypeQuery(innerQuery) => supertype(compileUnissonQuery(innerQuery))
            case _ => throw new IllegalArgumentException("Unknown query type: " + query)
        }
    }

    def createIncomingQuery(constraint:IncomingConstraint): LazyView[Violation] =
    {
        import sae.syntax.RelationalAlgebraSyntax._
        val dependencyRelation = kindAsDependency(constraint.kind)
        val targetQuery =  existingQuery(constraint.target).getOrElse(createEnsembleQuery(constraint.target))

        var query = ( (dependencyRelation, Queries.target(_)) ⋉ (identity(_:SourceElement[AnyRef]), targetQuery) ) ∩
        ( (dependencyRelation, Queries.source(_)) ⊳ (identity(_:SourceElement[AnyRef]), targetQuery) )

        constraint.sources.foreach(
            (source : Ensemble) =>
            {
                val sourceQuery = existingQuery(source).getOrElse(createEnsembleQuery(source))
                query = query ∩
                ((dependencyRelation, Queries.source(_)) ⊳ (identity(_:SourceElement[AnyRef]), sourceQuery))
            }
        )

        // TODO currently we do not resolve sources as ensembles for the constraint.
        // potentially the element can belong to more than one ensemble
        Π( (d:Dependency[AnyRef, AnyRef]) => Violation( None, Queries.source(d), Some(constraint.target), Queries.target(d), constraint, constraint.kind) )(query)
    }

    def createOutgoingQuery(constraint:OutgoingConstraint): LazyView[SourceElement[AnyRef]] =
    {
        null
    }

    def createNotAllowedQuery(constraint:NotAllowedConstraint): LazyView[SourceElement[AnyRef]] =
    {
        null
    }

    def createExpectedQuery(constraint:ExpectedConstraint): LazyView[SourceElement[AnyRef]] =
    {
        null
    }

/**
 * The follwoing depdency kinds are supported
 *  extends(Class1, Class2)
 *  implements(Class1, Class2)
 *  field_type(Field, Class)
 *  parameter(Method, Class)
 *  return_type(Method, Class)
 *  write_field(Method, Field)
 *  read_field(Method, Field)
 *  class_cast(Method, Class)
 *  instanceof(Method, Class)
 *  create(Method, Class)
 *  create_class_array(Method, Class)
 *  throw(Method, Class)
 *
 *  // TODO implement the following
 *  get_class(Method, Class)
 *  annotation(Class|Field|Method, Class)
 *  parameter_annotation(Method, Class)
 *  calls(Method1, Method2) =
 *  signature
 */
    private def kindAsDependency(kind : String) : LazyView[Dependency[AnyRef, AnyRef]] =
    {
        import sae.syntax.RelationalAlgebraSyntax._
        kind match
        {

            case "all" => checker.db.dependency.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "calls" => checker.db.calls.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "subtype" => checker.db.subtypes.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "signature" => checker.db.parameter.∪[Dependency[AnyRef, AnyRef], return_type] (checker.db.return_type)
            case "extends" => checker.db.`extends`.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "implements" => checker.db.implements.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "field_type" => checker.db.field_type.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "parameter" => checker.db.parameter.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "return_type" => checker.db.return_type.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "write_field" => checker.db.write_field.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "read_field" => checker.db.read_field.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_interface" => checker.db.invoke_interface.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_special" => checker.db.invoke_special.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_static" => checker.db.invoke_static.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "invoke_virtual" => checker.db.invoke_virtual.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "instanceof" => checker.db.instanceof.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "create" => checker.db.create.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "create_class_array" => checker.db.create_class_array.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "class_cast" => checker.db.class_cast.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "throws" => checker.db.thrown_exceptions.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
            case "exception" => checker.db.handled_exceptions.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
        }
    }

    private def existingQuery(c : DependencyConstraint) : Option[LazyView[Violation]] =
    {

        if( checker.hasConstraint(c) )
        {
            checker.violations(c)
        }
        None
    }

    private def existingQuery(e : Ensemble) : Option[LazyView[SourceElement[AnyRef]]] =
    {

        if( checker.hasEnsemble(e) )
        {
            checker.ensembleElements(e)
        }
        None
    }
}

