
# Parser example walkthrough

This section describes how the parser can be run within the Scala interpreter. The parser project is located /demo/chart-parser.

## Executing the parser

1. Open a terminal in the main directory of the project.
2. Move to the project folder of the parser

		$ cd /demo/chart-parser
		
3. Start the SBT-Scala console
		
		$ sbt console
			
	Now the Scala console is started with access to all classes of the parser. In order to use the parser consider the following example:
		
4. Import the parser.

		> import idb.demo.chartparser.SentenceParser 

5. Create a new value to have easier access to the parser

		> val parser = SentenceParser

6. Enter a sentence as list and zip the list with its indices. The zip is necessary since the parser expects tuples of words and their position in the sentence.

		> val words = List("green", "ideas", "sleep").zipWithIndex

7. Materialize the result relation in order to store its elements.

		> val result = parser.success.asMaterialized

8. Add the words to the parser.

		> words.foreach(parser.input.add)

9. Print the result relation 

		> result.foreach(println)
			
	The result relation stores the indices for all possible sub-sentences, i.e. the index of the first word and the index of the last word for every possible sentence. In our example, there are the pairs (0,2) and (1,2). The pair (0,2) says that there is a valid sentence from word 0 ("green") to word 2 ("sleep"), which is the whole sentence. That means that the input sentence is valid. Additionally, there is the edge (1,2). That means that there is also a valid sub-sentence, i.e. List("ideas", "sleep") would also be a valid sentence.
	
## Incremental addition

1. Add a new word to the sentence

		> parser.input += ("furiously", 3)
			
2. Print the result relation 

		> result.foreach(println)
			
	The result relation has been updated. The result relation contains the pair (0,3) and thus the whole sentence is valid.
	
## Incremental update

1. Update the first element from green to yellow

		> parser.input update (("green",0),("yellow",0))
			
2. Print the result relation 

		> result.foreach(println)

	As you can see, the pair (0,3) has disappeared from the result relation and thus the sentence is not valid anymore.	However, we can add a new rule to our grammar.	
		
3. Add the rule for adjective "yellow" to the grammar

		> parser.terminals += ("yellow", "Adj")
			
4. Print the result relation 

		> result.foreach(println)
			
	Now, the pair (0,3) is in our result relation and thus the sentence is valid.
		
		
# Executing analyses

This section contains step-by-step guides for executing the analyses described in the paper (Section 6). 

## Step-by-step instructions for executing a single analysis  and output the results

1. Open a terminal in the main directory of the project.

2. Execute sbt in the terminal

		$ sbt
			
3. Change project to the findbugs analyses

		> project analyses-findbugs
			
4. Run the analysis with its name as argument. The analysis will be executed on JDK 1.7.0 64-bit. The names of all analyses are shown in the list below (Section 7.3).

		> run-main sae.analyses.findbugs.AnalysisRunner FI_USELESS
	
## Step-by-step instructions for executing the analyses benchmarks:

1. Open a terminal in the main directory of the project.
	
2. Execute sbt in the terminal

		$ sbt
	
3. Change project to the analysis profiler

		> project analyses-profiler
			
4. Run the profiler with a properties file as argument. There is a properties file for every analysis, which is named <analysis-name>.properties, e.g. FI_USELESS.properties. Analysis names can be found in the list below (Section 7.3).

		> run-main sae.analyses.profiler.ASMDatabaseReplayTimeRunner FI_USELESS.properties
			
5. The results can be found as .csv file at /benchmarks/time/default/<analsis-name>.properties.csv 
			
## List of possible analyses

List of possible analyses (the intent of the analyses can be found at http://findbugs.sourceforge.net/bugDescriptions.html):

* BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION
* CI_CONFUSED_INHERITANCE
* CN_IDIOM
* CN_IDIOM_NO_SUPER_CALL
* CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
* CO_ABSTRACT_SELF
* CO_SELF_NO_OBJECT
* DM_GC
* DM_RUN_FINALIZERS_ON_EXIT
* DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT
* DP_DO_INSIDE_DO_PRIVILEGED
* EQ_ABSTRACT_SELF
* FI_PUBLIC_SHOULD_BE_PROTECTED
* FI_USELESS
* IMSE_DONT_CATCH_IMSE
* MS_PKGPROTECT
* MS_SHOULD_BE_FINAL
* NONE
* SE_BAD_FIELD_INNER_CLASS
* SE_NO_SUITABLE_CONSTRUCTOR
* SS_SHOULD_BE_STATIC
* SW_SWING_METHODS_INVOKED_IN_SWING_THREAD
* UG_SYNC_SET_UNSYNC_GET
* UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
* UUF_UNUSED_FIELD