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
	This module provides predicates to create, traverse and manipulate a 
	program's AST.
	
	<p><b>The AST</b><br/>
	Conceptually, the AST has the following structure:
	<pre><code>
	[										% The list of all predicates...
		pred(								% A predicate.
			a/1, 							% The identifier of the predicate.
			[								% The clauses defining the predicate... 
											% (built-in predicates do not have clauses)
				(	C, 					% A clause.
					[det, type=I]		% The properties of this clause.
				)
			],												
			[type=...]					% The properties of this predicate (e.g.,
											% whether this predicate – as a whole – is
											% deterministic).
		),...
	],
	[
											% The list of directives
	]
	</code></pre>
	This module provides all predicates that are necessary to process (parts of)
	the AST. <i>Code that wants to add, remove, manipulate or traverse the AST
	is not allowed to access the AST on its own.</i><br/> 
	<br/>
	The precise data structures that are used to store the AST and its nodes
	are an implementation detail of this module. This enables the
	exchange/evolution of the underlying data structure(s) whenever necessary.
	In particular, code that uses the AST must not make assumptions about
	the order and the way the predicates are stored, how clauses are stored, and
	in which way the properties of a clause or predicate are handled. <i>The 
	code must use the appropriate abstractions</i>.
	</p>
	
	<p><b>Used Terminology</b><br/>
	To refer to the different parts of a prolog program, we use the terminology
	as defined in the document "Structure of Prolog Programs."
	</p>

	@author Michael Eichberg
*/
:- module(
	'SAEProlog:Compiler:AST',
	[	
		empty_ast/1,
		variable/2,
		variable/3,
		variable/4,
		anonymous_variable/2,
		anonymous_variable/3,
		integer_atom/2,	
		integer_atom/3,
		float_atom/2,		
		float_atom/3,
		string_atom/2,
		string_atom/3,
		complex_term/3,		
		complex_term/4,
		complex_term/5,
		complex_term_args/2,
		complex_term_functor/2,		
		directive_goal/2,
		rule/4,
		rule_head/2,
		rule_body/2,
		variables_of_term/2,
		
		pos_meta/2,
		lookup_in_meta/2,
		term_meta/2,
		term_pos/2,
		term_pos/4,
		add_clause/3,
		add_predicates/3,
		is_rule/1,
		is_rule_with_body/1,
		is_rule_without_body/1,
		is_directive/1,
		is_variable/1,
		is_anonymous_variable/1,
		is_string_atom/1,
		is_integer_atom/1,
		is_float_atom/1,
		is_numeric_atom/1,
		
		user_predicate/2,
		predicate_identifier/2,
		predicate_clauses/2,
		%clause_n/2,
		foreach_clause/3,
		clause_definition/2,
		clause_meta/2,
		
		write_ast/2,
		write_clauses/1,
		write_ast_node/1
	]
).
:- meta_predicate(foreach_clause(+,3,-)).
:- meta_predicate(foreach_clause_impl(+,+,4,-)).

:- use_module('Utils.pl').




/**DEVELOPER
	Internal Structure of the AST:

	[										% The list of all predicates
		pred(								% A predicate.
			a/1, 							% The identifier of the predicate.
			[								% An open list of ...
				(							% ... the clauses defining the predicate 
					C, 
					[det,_]				% An open list of this clause's properties.
				),
				_
			],			
											
			[type=...,_]				% An open list of this predicate's properties.
		),...
	],
	[
		Directives (as is)
	]

*/



/**
	Unifies a given AST with the empty AST.
	
	@signature empty_ast(AST)
	@arg AST An empty AST.
*/
empty_ast(([]/*Rules*/,[]/*Directives*/)).



/**
	Adds a clause – which has to be a valid, normalized rule or a directive – to
	the given AST.

	@signature add_clause_to_ast(AST,Clause,NewAST)
	@arg(in) AST The AST to which the given term is added.
	@arg(in) Clause Clause has to be a valid, normalized rule or a directive.
	@arg(out) NewAST The new AST which contains the new term (predicate).
*/
add_clause(AST,Rule,NewAST) :- 
	Rule = ct(_Meta,':-',[LeftTerm,_RightTerm]),
	(	% e.g., terms such as "do :- write(...)".
		LeftTerm = a(_,Functor),
		Arity = 0
	;	% e.g., terms such as "do(Foo,Bar) :- write(Foo),write(Bar)".
		LeftTerm = ct(_,Functor,Args),
		length(Args,Arity)
	),!,
	ID = Functor/Arity,
	AST=(Rules,Directives),
	(	memberchk(pred(ID,Clauses,_PredProps1),Rules) ->
		append_ol(Clauses,(Rule,_ClauseProps1)),
		NewAST=AST
	;
		NewAST=([pred(ID,[(Rule,_ClauseProps2)|_],_PredProps2)|Rules],Directives)
	).
add_clause((Rules,Directives),Directive,NewAST) :-
	Directive = ct(_Meta,':-',[_TheDirective]),!,
	NewAST=(Rules,[Directive|Directives]).
add_clause(_AST,Term,_NewAST) :- 
	throw(internal_error('the term is not a rule or directive',Term)).



/**
	Adds the information about the built-in predicates to the AST.

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
		
	@arg(out) NewAST The extended AST.
*/
add_predicates((Rules,Directives),[pred(ID,Properties)|Preds],NewAST) :- !,
	IntermediateAST = ([pred(ID,[],Properties)|Rules],Directives),
	add_predicates(IntermediateAST,Preds,NewAST).
add_predicates(AST,[],AST).


/**
	@signature user_defined_predicates(AST,UserPredicateASTNode)
*/
user_predicate((Predicates,_Directives),UserPredicate) :-
	user_predicate_impl(Predicates,UserPredicate).
user_predicate_impl([Predicate|Predicates],UserPredicate) :-
	(
		Predicate = pred(_Identifier,[_|_],_PredicateProperties),
		UserPredicate = Predicate
	;
		user_predicate_impl(Predicates,UserPredicate)
	).


predicate_clauses(pred(_ID,Clauses,_Properties),Clauses).

/*
clause_n(Clauses,Clauses_N) :- clause_n_impl(Clauses,1,Clauses_N).
clause_n_impl([Clause|Clauses],N,[(N,Clause)|OtherClauses]):- !,
	NewN is N + 1,
	clause_n_impl(Clauses,NewN,OtherClauses).
clause_n_impl([],_,[]).
*/


foreach_clause(Clauses,F,Os) :- 
	foreach_clause_impl(Clauses,1,F,Os).
foreach_clause_impl(Var,_,_,[]) :- var(Var),!. % Clauses is an open list...
foreach_clause_impl([Clause|Clauses],N,F,[O|Os]) :- !,
	call(F,N,Clause,O),
	NewN is N + 1,
	foreach_clause_impl(Clauses,NewN,F,Os).


clause_definition((Clause,_Meta),Clause).


clause_meta((_Clause,Meta),Meta).



/**
 @signature predicate_identifier(Predicate_ASTNode,Functor/Arity)
*/
predicate_identifier(pred(ID,_Clauses,_Properties),ID).



/**
	Succeeds if the given ASTNode represents a directive.<br /> 
	(A directive is a complex term, with the functor 
	":-" and exactly one argument.)
	<p>
	A directive is never a rule(/fact) and vice versa.
	</p>
	
	@signature is_directive(ASTNode)
*/
is_directive(ct(_Meta,':-',[_Directive])) :- true. 


/**
	Goal is a directive's goal. For example, given the directive:
	<code><pre>
	:- use_module('Utils.pl').
	</pre></code>
	then Goal is the AST node that represents the complex term <code>use_module</code>.
	<p>
	This predicate can either be used to extract a directive's goal or
	to construct a new AST node that represents a directive and which is not
	associated with any meta information.
	</p>
	
	@signature directive(Directive_ASTNode,Goal_ASTNode)
	@arg Directive_ASTNode An AST node that represents a directive definition.
	@arg Goal_ASTNode The directive's goal
*/
directive_goal(ct(_Meta,':-',[Goal]),Goal) :- true.


/**
	(De-)constructs a rule.
	
	@signature rule(Head_ASTNode,Body_ASTNode,Meta,Rule_ASTNode)
	@arg Head_ASTNode The rule's head.
	@arg Body_ASTNode The rule's body.
	@arg Meta Meta-information about the rule.
	@arg(out) Rule_ASTNode
*/
rule(Head,Body,Meta,ct(Meta,':-',[Head,Body])) :- true.


/**
	Extracts a rule's head.
	<p>
	This predicate must not/can not be used to create a partial rule. The ASTNode
	will be invalid.
	</p>
	
	@signature rule_head(ASTNode,Head_ASTNode)
	@arg(in) ASTNode 
	@arg(out) Head_ASTNode A rule's head. 
*/
rule_head(ct(_Meta,':-',[Head,_]),Head) :- !.
rule_head(Head,Head) :- \+ is_directive(Head).



/**
	Extracts a rule's body.
	<p>
	This predicate must not/can not be used to create a partial rule. The ASTNode
	will be invalid.
	</p>

	@signature rule_body(Rule_ASTNode,RuleBody_ASTNode)
*/
rule_body(ct(_Meta,':-',[_Head,Body]),Body) :- true.


/**
	Succeeds if the given clause is a rule definition; i.e., if the clause is not
	a directive.

	@signature is_rule(Clause_ASTNode) 
*/
is_rule(Clause) :- is_rule_with_body(Clause),!.
is_rule(Clause) :- is_rule_without_body(Clause),!.


/**
	Succeeds if the given AST node represents a rule with a head and a body.
	
	@signature is_rule_with_body(ASTNode)
	@arg(in) ASTNode
*/
is_rule_with_body(ct(_Meta,':-',[_Head,_Body])) :- true.


/**
	Succeeds if the given AST node represents a rule definition without a body;
	e.g.,
	<code><pre>
	a_string_atom.
	a_term(X,Y).
	</pre></code>
	
	@signature is_rule_without_body(ASTNode)
	@arg(in) ASTNode
*/
is_rule_without_body(Clause) :- 
	\+ is_directive(Clause),
	(
		Clause = ct(_CTMeta,Functor,_Args),
		Functor \= (':-')
	;
		Clause = a(_AMeta,_Value) % e.g., "true."
	).


/**
	Succeeds if ASTNode represents the declaration of a named variable; i.e.,
	a variable that is not anonymous.
	
	@signature is_variable(ASTNode)
	@arg(in) ASTNode
*/
is_variable(v(_Meta,_Name)).


/**
	Succeeds if ASTNode represents the declaration of an anonymous variable.
	
	@signature is_anonymous_variable(ASTNode)
	@arg(in) ASTNode	
*/
is_anonymous_variable(av(_Meta,_Name)).


/**
	Succeeds if ASTNode represents an integer value/atom.
	
	@signature is_integer_atom(ASTNode)
	@arg(in) ASTNode	
*/
is_string_atom(a(_Meta,_Value)).


/**
	Succeeds if ASTNode represents a numeric value/atom.
	
	@signature is_numeric_atom(ASTNode)
	@arg(in) ASTNode	
*/
is_numeric_atom(ASTNode) :- 
	once((is_integer_atom(ASTNode) ; is_float_atom(ASTNode))).


/**
	Succeeds if ASTNode represents an integer value/atom.
	
	@signature is_integer_atom(ASTNode)
	@arg(in) ASTNode	
*/
is_integer_atom(i(_Meta,_Value)).


/**
	Succeeds if ASTNode represents a floating point value/atom.
	
	@signature is_float_atom(ASTNode)
	@arg(in) ASTNode	
*/
is_float_atom(r(_Meta,_Value)).



/**
	(De)constructs an AST node that represents a variable definition. If this 
	predicate is used to construct an AST node, the node will not be associated
	with any meta information.
	
	@signature variable(Variable_ASTNode,VariableName)
*/
variable(v(_Meta,VariableName),VariableName).

/**
	(De)constructs an AST node that represents a variable definition. If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.
	
	@signature variable(VariableName,Pos,Variable_ASTNode)
*/
variable(Name,Pos,v([Pos|_],Name)).



/**
	ASTNode is a term that represents a variable definition. 
	<p>
	The name of the variable is determined by concatenating the BaseQualifier and 
	the ID. The associated meta information (e.g., the position of the variable
	in the source code) is determined by Meta.<br />
	This predicate can only be used to create new variable nodes.
	</p>
	
	@signature variable_node(BaseQualifier,Id,Meta,ASTNode)
	@arg(atom) BaseQualifier Some atom.
	@arg(atom) Id Some atom.
	@arg Meta Meta-information associated with the variable.	
	@arg ASTNode An AST node representing the definition of a non-anonymous 
		variable.
*/
variable(BaseQualifier,Id,Meta,v(Meta,VariableName)) :-
	atom_concat(BaseQualifier,Id,VariableName).	


/**
	(De)constructs an AST node that represents the definition of an anonymous 
	variable.<br/>
	If this 
	predicate is used to construct an AST node, the node will not be associated
	with any meta information.

	@signature anonymous_variable(AnonymousVariable_ASTNode,Name)
*/
anonymous_variable(av(_Meta,Name),Name).


/**
	(De)constructs an AST node that represents the definition of an anonymous 
	variable.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.
	
	@signature anonymous_variable(Name,Pos,AnonymousVariable_ASTNode)
*/
anonymous_variable(Name,Pos,av([Pos|_],Name)).



/**
	(De)constructs an AST node that represents the definition of an integer 
	atom.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature integer_atom(IntegerAtom_ASTNode,Value)
*/
integer_atom(i(_Meta,Value),Value).


/**
	(De)constructs an AST node that represents the definition of an integer atom.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.
	
	@signature integer_atom(Value,Pos,IntegerAtom_ASTNode)
*/
integer_atom(Value,Pos,i([Pos|_],Value)).


/**
	(De)constructs an AST node that represents the definition of a float 
	atom.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature float_atom(FloatAtom_ASTNode,Value)
*/
float_atom(r(_Meta,Value),Value).

/**
	(De)constructs an AST node that represents the definition of a float atom.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.
	
	@signature float_atom(Value,Pos,FloatAtom_ASTNode)
*/
float_atom(Value,Pos,r([Pos|_],Value)).


/**
	(De)constructs an AST node that represents the definition of a string 
	atom.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature string_atom(StringAtom_ASTNode,Value)
*/
string_atom(a(_Meta,Value),Value).


/**
	(De)constructs an AST node that represents the definition of a string atom.<br />
	If this 
	predicate is used to construct an AST node, the only meta information will
	be the position information.

	@signature string_atom(Value,Pos,StringAtom_ASTNode)
*/
string_atom(Value,Pos,a([Pos|_],Value)).


/**
	(De)constructs an AST node that represents the definition of a complex term.<br/>
	If this predicate is used to construct an AST node, the node will not be
	associated with any meta information.
	
	@signature complex_term(ComplexTerm_ASTNode,Functor,Args)
	@arg ComplexTerm_ASTNode
	@arg Functor A string atom representing the complex term's functor.
	@arg Args the arguments of a complex term. Each argument is an AST node.
*/
complex_term(ct(_Meta,Functor,Args),Functor,Args).


/**
	Constructs a new complex term.
	
	@signature complex_term(Functor,Args,Pos,ASTNode)
	@arg Args the arguments of a complex term. They are AST nodes.
*/
complex_term(Functor,Args,Pos,ct([Pos|_],Functor,Args)).


/**
	Constructs a new complex term.
	
	@signature complex_term(Functor,Args,Pos,OperatorTable,ASTNode)
	@arg Args the arguments of a complex term. They are AST nodes.
*/
complex_term(Functor,Args,Pos,OperatorTable,ct([Pos,OperatorTable|_],Functor,Args)).


/**
	@signature complex_term_args(ComplexTermASTNode,Args)
	@arg(in) ComplexTermASTNode An AST node that represents a complex term.
	@arg(out) Args the arguments of a complex term. They are AST nodes.
*/
complex_term_args(ct(_Meta,_Functor,Args),Args).
complex_term_functor(ct(_Meta,Functor,_Args),Functor).


/**
	Meta is a term that represents the term's meta information. For example, the
	source code position, the current operator table, associated comments.
	@arg(in) ASTNode
	@arg(out) Meta
*/
term_meta(ASTNode,Meta) :- ASTNode =.. [_,Meta|_].


lookup_in_meta(Type,Meta) :- memberchk_ol(Type,Meta).


/**
	@signature meta_pos(Position,ASTNodeMetaInformation)
*/
pos_meta(Pos,[Pos|_]) :- Pos = pos(_,_,_).


/**
	Pos is the position of a term in the source file.
	
	@arg(in) ASTNode An AST node representing a term in a source file.
	@arg(out) Pos The position object {@file SAEProlog:Compiler:Parser} identifying
		the position of the term in the source file.
*/
term_pos(ASTNode,Pos) :- ASTNode =.. [_,[Pos|_]|_].


/**
	Pos is the position of a term in the source file.
	
	@arg(in) ASTNode An AST node representing a term in a source file.
	@arg(out) File 
	@arg(out) LN
	@arg(out) CN 	 
*/ % TODO deep documentation: make sure that the in annotation is enforced (=.. requires AST to be a complex term or an atom)
term_pos(ASTNode,File,LN,CN) :- ASTNode =.. [_,[pos(File,LN,CN)|_]|_].



variables_of_term(ASTNode,Vs) :- 
	variables_of_term(ASTNode,[],Vs).
/**
	@signature term_variables(ASTNode,Variables)
*/
variables_of_term(v(_,VariableName),Vs,NewVs) :- !,
	add_to_set(VariableName,Vs,NewVs).
variables_of_term(ct(_,_,Args),Vs,NewVs):-!,
	variables_of_terms(Args,Vs,NewVs).
variables_of_term(_,Vs,Vs).	


variables_of_terms([Term|Terms],Vs,NewVs) :-
	variables_of_term(Term,Vs,IVs),
	variables_of_terms(Terms,IVs,NewVs).
variables_of_terms([],Vs,Vs).
	


/* ************************************************************************** *\
 *                                                                            *
 *            P R E D I C A T E S   T O   D E B U G   T H E   A S T           *
 *                                                                            *
\* ************************************************************************** */

write_ast(ShowPredicates,(Rules,Directives)) :- 
	write_rules(ShowPredicates,Rules),
	write_directives(Directives).	

	
write_directives([Directive|Directives])	:-
	write_ast_node(Directive),nl,
	write_directives(Directives).
write_directives([]).


/**
	Writes the complete AST - including all meta-information - to standard out.

	@signature write_rules(ShowPredicates,AST) 
	@arg(in) ShowPredicates The kind of predicates that should be printed out.
		ShowPredicates has to be <code>built_in</code> or <code>user</code>.
	@arg(in) AST the ast.
*/	
write_rules(_ShowPredicates,[]) :- !. % green cut	
write_rules(ShowPredicates,[pred(Identifier,Clauses,PredicateProperties)|Predicates]) :-	
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
	write_rules(ShowPredicates,Predicates).	
write_rules(_ShowPredicates,AST) :- 
	% IMRPOVE throw exception in case of internal programming errors
	write('[Internal Error:AST:write_rules/2] The AST: '),
	write(AST),
	write(' is invalid.\nl').
	
	
/**
	Writes all information about the clauses of a predicate to standard out.

	@signature write_clauses(Clauses) 
	@arg(in) Clauses The (open) list of clauses defining a predicate.
*/	
write_clauses(Clauses) :- 
	ignore(
		(
		Clauses \= [],
		write('[Debug] Clauses: '),nl,
		write_clauses(1,Clauses)
		)
	).
	
% Implementation of write_clauses/1
write_clauses(_,Clauses) :- 
	var(Clauses)/* Clauses is an open list and a test such as Clauses = [] would
						lead to accidental unification. */,
	!.
write_clauses(Id,[(Clause,ClauseProperties)|Clauses]) :-
	write('[Debug]   '), write(Id), write(':\t'), write_ast_node(Clause),nl,
	write('[Debug]   '), write(Id), write(':\tProperties: '),
	( 	var(ClauseProperties) ->
		write('None')
	;
		write(ClauseProperties)
	), nl,
	NewId is Id + 1,
	write_clauses(NewId, Clauses).	
	
	

/**
	Writes a text representation of a given AST node to standard out.<br />
	The generated representation is meant to facilitate debugging the compiler
	and not as a representation of the AST.
	
	@signature write_ast_node(Term)
	@arg(in) Term A node in the AST representing a term in the source file.
		{@link Parser#:-(module)}
*/
%write_ast_node(v(_Meta,Name)) :- !, write('v('),write(Name),write(')').
write_ast_node(v(_Meta,Name)) :- !, write(Name).
%write_ast_node(av(_Meta,Name)) :- !, write('av('),write(Name),write(')').	
write_ast_node(av(_Meta,Name)) :- !,write(Name).	
write_ast_node(a(_Meta,Atom)) :- !,	write('\''),write(Atom),write('\'').	
write_ast_node(i(_Meta,Atom)) :- !,	write(Atom).	
write_ast_node(r(_Meta,Atom)) :- !,	write(Atom).	
write_ast_node(ct(_Meta,Functor,ClauseList)) :- !,
	write(' \''),write(Functor),write('\''),
	write('[ '),write_term_list(ClauseList),write(' ]').
write_ast_node(X) :- throw(internal_error('[AST] the given term has an unexpected type',X)).
	
	
/**
	Writes a text representation of each AST Node, which represents a term in 
	the source file, to standard out.<br />
	Position information are dropped to make the output easier to read.

	@signature write_term_list(TermList)
	@arg(in) TermList A non-empty list of AST nodes representing source-level
		terms.
*/	
write_term_list([ASTNode|ASTNodes]) :-
	write_ast_node(ASTNode),
	write_term_list_rest(ASTNodes).	
	
write_term_list_rest([]) :- !.
write_term_list_rest([ASTNode|ASTNodes]) :-
	write(','),write_ast_node(ASTNode),
	write_term_list_rest(ASTNodes).