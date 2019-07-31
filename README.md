# Table of Contents

1. Abstract
2. Tools
3. Install guide for i3Ql
4. Package Overview

# Abstract	
	
An incremental computation updates its result based on a change to its input, which is often an order of magnitude faster than a recomputation from scratch. In particular, incrementalization can make expensive computations feasible for settings that require short feedback cycles, such as interactive systems, IDEs, or (soft) real-time systems. 
	
We presented i3QL, a general-purpose programming language for specifying incremental computations. i3QL provides a declarative SQL-like syntax and is based on incremental versions of operators from relational algebra, enriched with support for general recursion. We integrated i3QL into Scala as a library, which enables programmers to use regular Scala code for non-incremental subcomputations of an i3QL query and to easily integrate incremental computations into larger software projects. To improve performance, i3QL optimizes user-defined queries by applying algebraic laws and partial evaluation.
	
# Tools
	
We implemented i3Ql in Scala (http://www.scala-lang.org). Scala is an object-oriented, functional programming language that run on the Java Virtual Machine (JVM). i3Ql queries can be directly used in your Scala code.

We use Lightweight Modular Staging (LMS, http://scala-lms.github.io) in order to compile our queries. LMS allows us to generate tree representations of functions and queries. This intermediate representation is used to optimize our queries. Further, LMS can produce Scala code out of these representations and compile the code during runtime. 

In order to built our project we use the SBT (http://www.scala-sbt.org), a build tool for Scala projects. 
	
# Install guide for i3QL

In order to install the project, you have to follow these steps:

1. To build this project you need to have JDK, SBT and GIT installed

2. Download and build the LMS project

		$ git clone https://github.com/TiarkRompf/virtualization-lms-core.git

	Go to the root directory and install the project using SBT. You need to be on the branch 'develop'

		$ cd virtualization-lms-core
		$ git checkout develop
		$ sbt publish-local

3. Download and build the i3Ql project.

		$ git clone https://github.com/seba--/i3QL.git

	Go to the root directory and install the project using SBT. Currently you need to be on the branch 'master' (the default one).

		$ cd i3Ql
		$ git checkout master
		$ sbt publish-local
	
## Using i3Ql in your own project
	
1. Add the dependencies to your SBT build file

	In order to use i3Ql for your own project you have to add the following library dependencies to your SBT build:		
			
		libraryDependencies in ThisBuild ++= Seq(
			"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
			"de.tud.cs.st" %% "idb-runtime" % "latest.integration"
		)
				
	Additionally, you have to add Scala Virtualized as Scala organization to your SBT build file:
			
		scalaVersion in ThisBuild := "2.10.2"
		scalaOrganization in ThisBuild := "org.scala-lang.virtualized"
		
2. Import i3Ql in your Scala project

	If you want to write a query, you have to write the following import statements
	
		import idb.syntax.iql._
		import idb.syntax.iql.IR._
	
	In order to use tables you also have to import them
	
		import idb.SetTable
		
3. Use i3Ql

	Now, you can use i3Ql.

	Example:

		def foo() {
			import idb.syntax.iql._
			import idb.syntax.iql.IR._
			import idb.SetTable

			//Create a new table. Note, that this has changed slightly from the paper.
			val table : Table[Int] = SetTable.empty[Int]()

			//Write your query.
			val query = SELECT (*) FROM table	

			//Add elements to the table.
			table += 1
		}

# Package Overview

This section gives an overview of all packages of the i3ql project.
	
Following are the packages of the i3Ql project in this repository:

* __idb__: Incremental Database
	* __idb-runtime__: Runtime classes/engine (e.g. operators) for relations
	* __idb-syntax-iql__: Syntax of the query language and transforming of queries into intermediate representation
	* __idb-intermediate-representation__: Representation and optimization of queries as syntax trees. Uses LMS.
	* __idb-runtime-compiler__: Transforms syntax trees to runnable "code"
	* __idb-runtime-distribution__: Implements the deployment and lifecycle of distributed queries 
	* __idb-integration-test__: End-to-end user tests of queries
	* __idb-schema-examples__: University database example that is used for testing
		
* __demo__: Contains several demo projects for standard i3QL. Please refer to [it's README](demo/README.md)

* __distributed-benchmarks__: Benchmarks for distributed i3QL and their shared implementation base
	* __company-benchmark__: Company case study
	* __hospital-benchmark__: Hospital case study
	* __tpch-benchmark__: TPC-H benchmark based benchmark and demo
	* _aws-setup_: Scripts for setup and execution of the benchmark on Amazon Web Service
	* _evaluation_: Scripts and templates for the evaluation of benchmark executions
