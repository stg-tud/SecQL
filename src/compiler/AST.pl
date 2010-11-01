/* License (BSD Style License):
   Copyright (c) 2010
   Department of Computer Science
   Technische Universität Darmstadt
   All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    - Neither the name of the Software Technology Group or Technische 
      Universität Darmstadt nor the names of its contributors may be used to 
      endorse or promote products derived from this software without specific 
      prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/


/**
	Predicates to create, traverse and manipulate an SAE Prolog program's AST.
	<p><b>The AST</b><br/>
	Conceptually, the AST has the following structure:
	<pre><code>
	[										% The list of all predicates
		pred(								% A predicate
			a/1, 							% The identifier of the predicate
			[								% The clauses defining the predicate 
											% (built-in predicates do not have clauses)
				(	C, 					% The clause
					[det, type=I]		% The properties of this clause
				)
			],												
			[type=...]					% The properties of this predicate (e.g.,
											% whether this predicate as a whole is
											% deterministic)
		),...
	]
	</code></pre>
	<b>However, code that wants to add, remove, manipluate or traverse the AST
	is not allowed to access the AST on its own.</b><br />
	This module provides all abstractions that are necessary to process
	the AST and the precise data structures that are used to store the AST
	are an implementation detail of this module. This enables us to easily 
	exchange the underlying data structure(s) whenever necessary. <br />
	In particular, code that uses the AST must not make assumptions about
	the order and the way the predicates are stored, how clauses are stored, and
	in which way the properties of a clause or predicate are handled. <i>The 
	code must use the appropriate abstractions</i>.<br />
	</p>
	<p><b>Used Terminology</b><br/>
	We use the following terminology to refer to the different parts of Prolog
	programs.
	<ul>
	<li><u>Atom</u> - An (integer/string) atom.</li>
	<li><u>Variable</u> - A prolog variable.</li>	
	<li><u>Anonymous Variable</u> - A variable starting with an underscore. 
		(Recall, all anonymous variables of a clause are unique, even if they 
		share the same name.)</li>	
	<li><u>Term</u> - A prolog term; i.e., some atom or complex term.</li>
	<li><u>Top-level Term</u> - A term that is defined at the top-level in a 
		source file.</li>	
	<li><u>Complex Term</u> - A term with a functor and at least one argument 
		(arity > 0). </li>
	<li><u>Directive</u> - A top-level complex term where the identifier is 
		':-'/1.</li>
	<li><u>Clause</u> - A top-level complex term where the identifier is 
		':-'/2.</li>
	<li><u>Functor</u> - The name of a term.</li>
	<li><u>Arity</u> - The number of arguments of a term. An integer atom equal 
		or larger than 0.</li>
	<li><u>Predicate</u> - A predicate is defined by the clauses that have the 
		same identifier.</li>
	<li><u>Fact</u> - A top-level complex term where the identifier is not
		':-'/2.</li>
	<li><u>Identifier</u> - Identifies each predicate uniquely. It is a term
		with the structure: <code>&lt;Functor&gt;/&lt;Arity&gt;</code>
	</li>	
	</ul>
	</p>

	@author Michael Eichberg
*/
:- module(
	'SAEProlog:Compiler:AST',
	[	
		new_ast/1,
		add_term_to_ast/3,
		add_predicates_to_ast/3,
		
		write_ast/2,
		write_clauses/1
	]
).


:- use_module('Utils.pl').



/* Internal Structure of the AST:

	<pre><code>
	[										% The list of all predicates
		pred(								% A predicate
			a/1, 							% The identifier of the predicate
			[								% Open list of ...
				(							% ... the clauses defining the predicate 
					C, 
					[det,_]				% Open list of this clauses properties
				),
				_
			],			
											
			[type=...,_]				% Open list of the properties of this predicate
		),...
	]
	</code></pre>
*/



/**
	Creates a new (empty) AST.
	
	@signature new_ast(AST)
	@arg(out) AST A new empty AST.
*/
new_ast([]).



/**
	Adds a top-level term, which is a valid, normalized clause, to the ast.

	@signature add_term_to_ast(AST,Term,NewAST)
	@arg(in) AST The AST to which this term should be added.
	@arg(in) TLTerm TLTerm has to be a valid normalized, top-level complex term; i.e.,
		no variable, no integer atom, no syntactically invalid complex term such as
		"X(a,b)" etc.
	@arg(out) NewAST The new AST which contains the new term (predicate).
*/
add_term_to_ast(AST,TLTerm,NewAST) :- 
	TLTerm = ct(_Pos,':-',[LeftTerm,_RightTerm]),
	(	% e.g., terms such as "do :- write(...)".
		LeftTerm = a(_,Functor),
		Arity = 0
	;	% e.g., terms such as "do(Foo,Bar) :- write(Foo),write(Bar)".
		LeftTerm = ct(_,Functor,Args),
		length(Args,Arity)
	),!,
	ID = Functor/Arity,
	(	memberchk(pred(ID,Clauses,_PredProps1),AST) ->
		append_ol(Clauses,(TLTerm,_ClauseProps1)),
		NewAST=AST
	;
		NewAST=[pred(ID,[(TLTerm,_ClauseProps2)|_],_PredProps2)|AST]
	).
add_term_to_ast(AST,Term,AST) :- 
	write('[Internal Error:AST:add_term_to_ast/3] The term does not seem to be a normalized top-level term: '),
	write(Term),
	nl.


/**
	Adds information about built-in predicates to the AST.

	@signature add_predicates_to_ast(AST,Predicates,NewAST)
	@arg(in) AST The current AST.
	@arg(in) Predicates The list of all built-in predicates. The structure is
		as follows:<br/>
		<pre><code>
		[									% List of predicates
			pred(							% A predicate
				Functor/Arity,			% The predicate's identifier
				[							% List of properties
					&lt;Property&gt;	% A property
				]
			)
		]
		</code></pre>
		
	@arg(out) NewAST The extended NewAST.
*/
add_predicates_to_ast(AST,[pred(ID,Properties)|Preds],NewAST) :- !,
	IntermediateAST = [pred(ID,[],Properties)|AST],
	add_predicates_to_ast(IntermediateAST,Preds,NewAST).
add_predicates_to_ast(AST,[],AST).





/* ************************************************************************** *\
 *                                                                            *
 *            P R E D I C A T E S   T O   D E B U G   T H E   A S T           *
 *                                                                            *
\* ************************************************************************** */
	

/**
	Writes the AST to standard out.

	@signature write_ast(ShowPredicates,AST) 
	@arg(in) ShowPredicates The kind of predicates that should be printed out.
		ShowPredicates has to be built_in or user.
	@arg(in) AST the ast.
*/	
write_ast(_ShowPredicates,[]) :- !. % green cut	
write_ast(ShowPredicates,[pred(Identifier,Clauses,PredicateProperties)|Predicates]) :-	
	(
		(	(ShowPredicates = built_in, Clauses = [])
		;
			(ShowPredicates = user, Clauses = [_|_]/* at least one element */)
		) -> (
			nl,write('[Debug] '),write(Identifier),nl,
			write('[Debug] Properties: '),
			( 	var(PredicateProperties) ->
				write('None')
			;
				write(PredicateProperties)
			),
			nl,
			write_clauses(Clauses)
		) 
	; 
		true
	),
	write_ast(ShowPredicates,Predicates).	
write_ast(_ShowPredicates,AST) :- 
	write('[Internal Error:AST:write_ast/2] The AST: '),
	write(AST),
	write(' is invalid.\nl').
	
	
/**
	Writes all information about the clauses of a predicate to standard out.

	@signature write_clauses(Clauses) 
	@arg(in) Clauses The list of clauses of a predicate.
*/	
write_clauses(Clauses) :- 
	ignore(
		(
		Clauses \= [],
		write('[Debug] Clauses: '),nl,
		write_clauses(1,Clauses)
		)
	).
% PRIVATE (Implementation of write_clauses/1)
write_clauses(_,Clauses) :- 
	var(Clauses)/* Clauses is an open list and a test such as Clauses = [] would
						lead to accidental unification. */,
	!.
write_clauses(Id,[(Clause,ClauseProperties)|Clauses]) :-
	write('[Debug]   '),write(Id),write(':\t'),	write_term(Clause,[quoted(true)]),nl,
	write('[Debug]   '),write(Id),write(':\tProperties: '),
	( 	var(ClauseProperties) ->
		write('None')
	;
		write(ClauseProperties)
	),nl,
	NewId is Id + 1,
	write_clauses(NewId, Clauses).	
	
	