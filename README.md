# Table of Contents

1. Abstract
2. Tools
3. Install guide for SecQL
4. Package Overview

# Abstract	
	
Distributed query processing is an effective means for processing large amounts of data. To abstract from the technicalities of distributed systems, algorithms for operator placement automatically distribute sequential data queries over the available processing units. However, current algorithms for operator placement focus on performance and ignore privacy concerns that arise when handling sensitive data.

We present a new methodology for privacy-aware operator placement that both prevents leakage of sensitive information and improves performance. Crucially, our approach is based on an information-flow type system for data queries to reason about the sensitivity of query subcomputations. Our solution unfolds in two phases. First, placement space reduction generates deployment candidates based on privacy constraints using a syntax-directed transformation driven by the information-flow type system. Second, constraint solving selects the best placement among the candidates based on a cost model that maximizes performance. We verify that our algorithm preserves the sequential behavior of queries and prevents leakage of sensitive data. We implemented the type system and placement algorithm for a new query language SecQL and demonstrate significant performance improvements in benchmarks.

For more detailed information, read our OOPSLA19 paper: https://doi.org/10.1145/3360593
	
# Tools
	
We implemented SecQL in Scala (http://www.scala-lang.org). Scala is an object-oriented, functional programming language that run on the Java Virtual Machine (JVM). i3Ql queries can be directly used in your Scala code.

SecQL is based on i3QL, which uses Lightweight Modular Staging (LMS, http://scala-lms.github.io) in order to compile our queries. LMS allows us to generate tree representations of functions and queries. This intermediate representation is used to optimize our queries. Further, LMS can produce Scala code out of these representations and compile the code during runtime.

In order to built our project we use the SBT (http://www.scala-sbt.org), a build tool for Scala projects.
	
# Install guide for SecQL

In order to install the project, you have to follow these steps:

1. To build this project you need to have JDK, SBT and GIT installed

2. Download and build the LMS project

	$ git clone https://github.com/DSoko2/virtualization-lms-core.git
	
	Checkout the tag "i3QL-compatible-scala2.11". Please note that is is required for SecQL to operate, since it is unfortunately not compatible with recent LMS versions.

	Go to the root directory and install the project using SBT. You need to be on the tag "i3QL-compatible-scala2.11"

		$ cd virtualization-lms-core
		$ git checkout develop
		$ sbt publish-local
	
	Note that there is also a pre-build docker image availble on dockerhub, in which the correct LMS version is installed and ready to use: https://hub.docker.com/r/dsoko2/i3ql-lms

3. Download and build the SecQL project.

		$ git clone https://github.com/stg-tud/SecQL.git

	Go to the root directory and install the project using SBT. Currently you need to be on the branch 'master' (the default one).

		$ cd SecQL
		$ git checkout master
		$ sbt publish-local
	
## Using SecQL in your own project
	
1. Add the dependencies to your SBT build file

	In order to use SecQL for your own project you have to add the following library dependencies to your SBT build:		
			
		libraryDependencies in ThisBuild ++= Seq(
			"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
			"de.tud.cs.st" %% "idb-runtime" % "latest.integration"
		)
				
	Additionally, you have to add Scala Virtualized as Scala organization to your SBT build file:
			
		scalaVersion in ThisBuild := "2.10.2"
		scalaOrganization in ThisBuild := "org.scala-lang.virtualized"
		
2. Use SecQL
	
	Just have a look at the distributed benchmarks, which can be found under ditributed-benchmarks/{hospital,company}. Also distributed-benchmarks/README.md might provide some useful information.

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
		
* __distributed-benchmarks__: Benchmarks for distributed i3QL and their shared implementation base
	* __company__: Company case study
	* __hospital__: Hospital case study
	* _aws-setup_: Scripts for setup and execution of the benchmark on Amazon Web Service
	* _evaluation_: Scripts and templates for the evaluation of benchmark executions
