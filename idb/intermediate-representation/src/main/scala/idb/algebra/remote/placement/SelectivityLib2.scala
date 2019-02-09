package idb.algebra.remote.placement

object SelectivityLib2 {

	val companyIterations = 100000

	/**
	  * Maps selectivity libs (Operator query hash -> selectivity) to placement id root query relation
	  */
	val libs: Map[String, Map[Int, Float]] = Map(

		/**
		  * CompanyBenchmark1
		  * root							1108862841
		  * projection						121057438
		  * equiJoin						1839901817
		  * equiJoin	reclass				-1291827807	-1267080172
		  * selection	reclass		actor	300609586	196037679	-604596560
		  * reclass		actor				818387364	-1413428089
		  * actor							-1669410282
		  */
		"company-query1" -> Map(
			1108862841 -> 1f,
			121057438 -> 1f,
			1839901817 -> 2f / (2f * companyIterations + 2f),
			-1291827807 -> 2f / (2f * companyIterations + 1f), -1267080172 -> 1f,
			300609586 -> 1f / (2f * companyIterations), 196037679 -> 1f, -604596560 -> 1f,
			818387364 -> 1f, -1413428089 -> 1f,
			-1669410282 -> 1f
		),

		/**
		  * CompanyBenchmark2
		  * root											630948699
		  * duplicateElimination							-2051140361
		  * projection										717264670
		  * equiJoin										-1524896686
		  * equiJoin	equiJoin							280778412	-504094756
		  * reclass		reclass		reclass		reclass		361891654	-1446155182	818387364	196037679
		  * actor		actor		actor		actor		-2008924253	-1723995261	-1669410282	-1413428089
		  */
		"company-query2" -> Map(
			630948699 -> 1f,
			-2051140361 -> 2f / 3f,
			717264670 -> 1f,
			-1524896686 -> 0.6f,
			280778412 -> 2f / 3f, -504094756 -> 0.6f,
			361891654 -> 1f, -1446155182 -> 1f, 818387364 -> 1f, 196037679 -> 1f,
			-2008924253 -> 1f, -1723995261 -> 1f, -1669410282 -> 1f, -1413428089 -> 1f
		),

		/**
		  * CompanyBenchmark3
		  * root											-564704707
		  * projection										1397582691
		  * equiJoin										114914612
		  * duplicateElimination	duplicateElimination	-1276114402	-1525350814
		  * projection				reclass					844084837	-67715869
		  * equiJoin				actor					627847485	1779553638
		  * declass					selection				1327865046	18173299
		  * projection				reclass					1088486325	328675783
		  * equiJoin				actor					705814719	1313771839
		  * equiJoin				reclass					-8642126	937862449
		  * equiJoin				reclass			actor	-343800074	-875665229	369114374
		  * equiJoin				reclass			actor	188694397	818387364	-444992408
		  * selection				reclass			actor	-1690858118	196037679	-1669410282
		  * reclass					actor					-1267080172	-1413428089
		  * actor											-604596560
		  */
		"company-query3" -> Map(
			-564704707 -> 1f,
			1397582691 -> 1f,
			114914612 -> 0.9f / 1.9f,
			-1276114402 -> 1f / 3f, -1525350814 -> 1f,
			844084837 -> 1f, -67715869 -> 1f,
			627847485 -> 2.25f, 1779553638 -> 1f,
			1327865046 -> 1f, 18173299 -> 0.9f,
			1088486325 -> 1f, 328675783 -> 1f,
			705814719 -> 0.75f, 1313771839 -> 1f,
			-8642126 -> 0.6f, 937862449 -> 1f,
			-343800074 -> 0.6f, -875665229 -> 1f, 369114374 -> 1f,
			188694397 -> 0.6f, 818387364 -> 1f, -444992408 -> 1f,
			-1690858118 -> 1f, 196037679 -> 1f, -1669410282 -> 1f,
			-1267080172 -> 1f, -1413428089 -> 1f,
			-604596560 -> 1f
		),

		/**
		  * CompanyBenchmark4
		  * root								-8642126
		  * projection							1650301162
		  * equiJoin							-1138252342
		  * selection	equiJoin				-1248427562	407303844
		  * reclass		selection	selection	-1267080172	1816991840	-1291827807
		  * actor		reclass		reclass		-604596560	361891654	-1446155182
		  * actor		actor					-2008924253	-1723995261
		  */
		"company-query4" -> Map(
			-8642126 -> 1f,
			1650301162 -> 1f,
			-1138252342 -> 0.2f / 2.2f,
			-1248427562 -> 1f, 407303844 -> 1f / 3f,
			-1267080172 -> 1f, 1816991840 -> 0.2f, -1291827807 -> 0.2f,
			-604596560 -> 1f, 361891654 -> 1f, -1446155182 -> 1f,
			-2008924253 -> 1f, -1723995261 -> 1f
		),

		/**
		  * CompanyBenchmark5
		  * root												-2128695709
		  * projection											1734285796
		  * equiJoin											2044335465
		  * equiJoin					selection				1550151830	1734365008
		  * aggregationSelfMaintained	reclass		reclass		-1561146018	-875665229	818387364
		  * reclass						actor		actor		328675783	-444992408	-1669410282
		  * actor												1313771839
		  */
		"company-query5" -> Map(
			-2128695709 -> 1f,
			1734285796 -> 1f,
			2044335465 -> 0.5f,
			1550151830 -> 1f, 1734365008 -> 0.5f,
			-1561146018 -> 0.1f, -875665229 -> 1f, 818387364 -> 1f,
			328675783 -> 1f, -444992408 -> 1f, -1669410282 -> 1f,
			1313771839 -> 1f
		),

		/**
		  * CompanyBenchmark6
		  * root						-1561146018
		  * projection					909673808
		  * crossProduct				280778412
		  * selection		selection	459859312	795854755
		  * reclass			reclass		937862449	818387364
		  * actor			actor		369114374	-1669410282
		  */
		"company-query6" -> Map(
			-1561146018 -> 1f,
			909673808 -> 1f,
			280778412 -> Math.min(1000f, (0.5f * companyIterations * 0.2f * companyIterations) / (0.7f * companyIterations)),
			459859312 -> 0.5f, 795854755 -> 0.1f,
			937862449 -> 1f, 818387364 -> 1f,
			369114374 -> 1f, -1669410282 -> 1f
		),

		/**
		  * CompanyBenchmark7
		  * root											-2128695709
		  * projection										1734285796
		  * equiJoin										506441734
		  * equiJoin	duplicateElimination				2044335465	-1743610656
		  * reclass		reclass					projection	818387364	-875665229	-1752384789
		  * actor		actor					selection	-1669410282	-444992408	1200707644
		  * reclass											937862449
		  * actor											369114374
		  */
		"company-query7" -> Map(
			-2128695709 -> 1f,
			1734285796 -> 1f,
			506441734 -> 0.4f,
			2044335465 -> 0.5f, -1743610656 -> 1f,
			818387364 -> 1f, -875665229 -> 1f, -1752384789 -> 1f,
			-1669410282 -> 1f, -444992408 -> 1f, 1200707644 -> 0.5f,
			937862449 -> 1f,
			369114374 -> 1f
		),

		/**
		  * CompanyBenchmark8
		  * root					1333852911
		  * unionAdd				-1418533009
		  * projection	projection	673303420		153402694
		  * selection	selection	-2004671778		-595414228
		  * reclass		reclass		361891654		361891654
		  * actor		actor		-2008924253		-2008924253
		  */
		"company-query8" -> Map(
			1333852911 -> 1f,
			-1418533009 -> 1f,
			673303420 -> 1f, 153402694 -> 1f,
			-2004671778 -> 0.2f, -595414228 -> 0.2f,
			361891654 -> 1f,
			-2008924253 -> 1f
		),

		/**
		  * CompanyBenchmark9
		  * root										-1423541663
		  * projection									1200707644
		  * equiJoin									-56228886
		  * intersection	reclass						-691368213	361891654
		  * projection		projection		actor		2033302757	1749213749	-2008924253
		  * selection		equiJoin					-871876039	1151247936
		  * reclass			reclass			selection	361891654	-1446155182	357687088
		  * actor			actor			declass		-2008924253	-1723995261	1468971770
		  * reclass										-1267080172
		  * actor
		  */
		"company-query9" -> Map(
			-1423541663 -> 1f,
			1200707644 -> 1f,
			-56228886 -> 0.2f / 1.2f,
			-691368213 -> 0.2f / 2.2f, 361891654 -> 1f,
			2033302757 -> 1f, 1749213749 -> 1f, -2008924253 -> 1f,
			-871876039 -> 0.2f, 1151247936 -> 0.5f,
			361891654 -> 1f, -1446155182 -> 1f, 357687088 -> 1f,
			-2008924253 -> 1f, -1723995261 -> 1f, 1468971770 -> 1f,
			-1267080172 -> 1f,
			-604596560 -> 1f
		),

		/**
		  * CompanyBenchmark10
		  * root											485213456
		  * projection										-2028918784
		  * equiJoin										121057438
		  * duplicateElimination	duplicateElimination	1752993660	-611495304
		  * projection				reclass					-1825762326	818387364
		  * equiJoin				actor					188694397	-1669410282
		  * selection				reclass					-1690858118	196037679
		  * reclass					actor					-1267080172	-1413428089
		  * actor											-604596560
		  */
		"company-query10" -> Map(
			485213456 -> 1f,
			-2028918784 -> 1f,
			121057438 -> 0.5f,
			1752993660 -> 2f / 3f, -611495304 -> 1f,
			-1825762326 -> 1f, 818387364 -> 1f,
			188694397 -> 0.6f, -1669410282 -> 1f,
			-1690858118 -> 1f, 196037679 -> 1f,
			-1267080172 -> 1f, -1413428089 -> 1f,
			-604596560 -> 1f
		)
	)
}
