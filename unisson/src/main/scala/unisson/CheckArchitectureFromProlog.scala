package unisson

import prolog.parser.UnissonPrologParser
import java.io._
import java.lang.IllegalArgumentException
import prolog.utils.ISOPrologStringConversion
import unisson.ast._
import sae.bytecode.BytecodeDatabase
import unisson.queries.QueryCompiler
import Utilities._

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 09:53
 *
 */

object CheckArchitectureFromProlog
{


    private val ensembleFunctor = "ensemble"

    private val dependencyConstraintFunctors = List("incoming", "outgoing", "not_allowed", "inAndOut", "expected")

    private val parser = new UnissonPrologParser()

    private val sadListOption = "--sad"

    private val jarListOption = "--code"

    private val ensembleOutputOption = "--outEnsembles"

    private val violationOutputOption = "--outViolations"

    private val classPattern = """.*\.class""".r

    private val jarPattern = """.*\.jar""".r

    private val usage = ("""CheckArchitectureFromProlog [<sadFile> <codeLocation>]
                |CheckArchitectureFromProlog [""" + sadListOption + """ [<sadFileList>] | <sadFile>] [""" + jarListOption + """ [<codeLocationList>] | <codeLocation>] [""" + ensembleOutputOption + """ <csvFile>] [""" + violationOutputOption + """ <csvFile>]
                |<sadFile>: A sad file architecture definition. Implicitly a .sad.pl file assumed to be present
                |<codeLocation>: A code location may be one of the following:
                |                - a jar file
                |                - .class file
                |<sadFileList> : A whitespace separated list of sad files
                |<jarFileList> : A whitespace separated list of jar files
                |<csvFile>     : A comma separated value file where output is written to
                """).stripMargin
    //TODO make directories a code location
    //                |                - a directory, which is searched recursively for .class files

    def main(args: Array[String])
    {
        if (args.length == 0) {
            println(usage)
            return
        }
        var sadFiles: Array[String] = Array()

        var codeLocations: Array[String] = Array()

        if (args(0) == sadListOption) {
            sadFiles = args.dropRight(1).drop(1).takeWhile(
                    (s: String) =>
                    s != jarListOption && s != violationOutputOption && s != ensembleOutputOption
            )

        }
        else {
            sadFiles = Array(args.head)
        }

        val rest = args.drop(sadFiles.size)

        if (rest.length == 0) {
            println("Not enough arguments -- specified " + sadFiles.size + "sad files and no code locations")
        }



        if (rest(0) == jarListOption) {
            codeLocations = rest.drop(1).takeWhile(
                    (s: String) =>
                    s != jarListOption && s != violationOutputOption && s != ensembleOutputOption
            )
        }
        else {
            codeLocations = Array(rest.head)
        }

        implicit val checker = checkArchitectures(sadFiles, codeLocations)

        implicit val delimiter = ";"


        println(
            "Ensemble;EnsembleElementCount;ConstraintTypem;ConstraintKind;Constraint(Srcs/Trgts);ConstraintViolationCount"
        )
        (checker.getEnsembles.toList.sortBy{ _.name}).foreach((e: Ensemble) => println(ensembleToString(e)))

        checker.ensembleElements( checker.getEnsemble("Exceptions").get ).foreach(println)

        println("-----------------------------------------------------------------")
        //checker.ensembleElements( checker.getEnsemble("@rest").get ).foreach(println)

        //checker.violations.foreach((v: Violation) => println(violationToString(v)))
    }


    def checkArchitectures(sadFiles: Array[String], codeLocations: Array[String]): ArchitectureChecker =
    {
        val database = new BytecodeDatabase
        val checker = new ArchitectureChecker(database)
        val compiler = new QueryCompiler(checker)

        sadFiles.foreach(
                (sadFile: String) => {
                val plFile = sadFile + ".pl"
                println("reading architecture from " + plFile)
                compiler.addAll(
                    readSadFile(
                        fileNameAsStream(plFile)
                    )
                )
            }
        )
        compiler.finishOutgoing()


        codeLocations.map(
                (loc: String) => loc match {
                case classPattern() => {
                    println("reading bytecode from " + loc)
                    database.transformerForClassfileStream(fileNameAsStream(loc)).processAllFacts()
                }
                case jarPattern() => {
                    println("reading bytecode from " + loc)
                    database.transformerForArchiveStream(fileNameAsStream(loc)).processAllFacts()
                }
                case _ => println("unrecognized code location type : " + loc)
            }
        )

        checker
    }


    def readSadFile(stream: InputStream): Seq[UnissonDefinition] =
    {
        val in = new BufferedReader(new InputStreamReader(stream))
        var result: Seq[UnissonDefinition] = Nil
        while (in.ready) {

            val s = in.readLine();
            if (s.trim().length() > 0 && !s.trim().startsWith("%") && !s.trim().startsWith(":-")) {
                result = result :+ readPrologLine(s)
            }
        }

        ResolveAST(result)
    }

    def resourceAsStream(name: String) =
    {
        this.getClass.getClassLoader.getResourceAsStream(name)
    }

    def fileNameAsStream(name: String) =
    {
        val file = new File(name)
        new FileInputStream(file)
    }


    def readPrologLine(s: String): UnissonDefinition =
    {
        // TODO move functor recognition to the parser
        val functor = ISOPrologStringConversion.getFunctor(s)
        if (functor == ensembleFunctor) {
            return readEnsemble(s)
        }
        else if (dependencyConstraintFunctors.contains(functor)) {
            return readDependencyConstraint(s)
        }
        throw new IllegalArgumentException("can not parse the following string: " + s)
    }

    def readEnsemble(s: String): UnresolvedEnsemble =
    {
        val result = parser.parseAll(parser.ensemble, s)
        result match {
            case parser.Failure(msg, next) => {
                println("unable to parse ensemble:")
                println(msg)
                println(next.pos.longString)
                System.exit(-1)
                null
            }
            case parser.Success(ensemble, _) => ensemble
        }
    }

    def readDependencyConstraint(s: String): DependencyConstraint =
    {
        val result = parser.parseAll(parser.dependencyConstraint, s)
        result.get
    }


}