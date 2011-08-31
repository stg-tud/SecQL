package unisson.prolog

import parser.UnissonPrologParser
import utils.ISOPrologStringConversion
import java.io._
import java.lang.IllegalArgumentException
import unisson.ast._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 09:53
 *
 */

object CheckArchitectureFromProlog {


    private val ensembleFunctor = "ensemble"

    private val dependencyConstraintFunctors = List("incoming", "outgoing", "not_allowed", "inAndOut", "expected")

    private val parser = new UnissonPrologParser()

    def main (args: Array[String]) {

        args.foreach((s:String) => readSadFile(new FileInputStream(s)))

    }

    def readSadFile( stream : InputStream ) : Seq[UnissonDefinition] =
    {
        val in = new BufferedReader(new InputStreamReader(stream))
        var result : Seq[UnissonDefinition] = Nil
        while( in.ready )
        {

            val s = in.readLine();
            if( s.trim().length() > 0 && ! s.trim().startsWith("%") && ! s.trim().startsWith(":-") )
            {
                result = result :+ readPrologLine(s)
            }
        }

        ResolveAST(result)
    }

    def resourceAsStream(name : String) = {
        this.getClass.getClassLoader.getResourceAsStream(name)
    }


    def readPrologLine(s: String) : UnissonDefinition =
    {
        // TODO move functor recognition to the parser
        val functor = ISOPrologStringConversion.getFunctor(s)
        if( functor == ensembleFunctor )
        {
            return readEnsemble(s)
        }
        else if( dependencyConstraintFunctors.contains(functor) )
        {
            return readDependencyConstraint(s)
        }
        throw new IllegalArgumentException("can not parse the following string: " + s)
    }

    def readEnsemble(s : String) : Ensemble =
    {
        val result = parser.parse(parser.ensemble, s)
        result.get
    }

    def readDependencyConstraint(s : String) : DependencyConstraint =
    {
        val result = parser.parse(parser.dependencyConstraint, s)
        result.get
    }


}