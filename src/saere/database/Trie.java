package saere.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class Trie {

	private final Term label; // will be an atom or a variable
	private final Trie parent; // required for trie iterator
	private Term term;
	private Trie firstChild;
	private Trie nextSibling;

	/**
	 * Creates a root trie (label is <tt>null</tt>, parent is <tt>null</tt>).
	 * 
	 * @param label
	 */
	public Trie() {	
		this(null, null);
	}
	
	private Trie(Term label, Trie parent) {
		assert label != null && (label.isIntegerAtom() || label.isStringAtom()) : "Invalid label";
		
		this.label = label;
		this.parent = parent;
		term = null;
		firstChild = nextSibling = null;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public Term getLabel() {
		return label;
	}

	public Term getTerm() {
		return term;
	}
	
	public Trie getParent() {
		return parent;
	}

	public Trie getFirstChild() {
		return firstChild;
	}

	public Trie getNextSibling() {
		return nextSibling;
	}

	public Trie add(Term term) {
		assert isRoot() : "Can add to root only";
		
		return add(new TermStack(breakDown(term).toArray(new Term[0])), term);
	}

	private Trie add(TermStack ts, Term t) {
		Term first = ts.peek();
		
		assert first != null && (first.isIntegerAtom() || first.isStringAtom() || first.isVariable()) : "Invalid first";
		
		if (isRoot()) {
			// add to own subtrie
			if (firstChild == null) {
				firstChild = new Trie(first, this);
			}
			return firstChild.add(ts, t);
		} else if (same(first, label)) {
			// the labels match
			ts.pop();

			// anything left to process?
			if (ts.size() == 0) {
				term = t;
				return this;
			}

			// add to own subtrie
			if (firstChild == null) {
				firstChild = new Trie(ts.peek(), this);
			}
			return firstChild.add(ts, t);
		} else {
			// add to sibling subtrie
			if (nextSibling == null) {
				nextSibling = new Trie(first, parent);
			}
			return nextSibling.add(ts, t);
		}
	}

	// or unify(Term ... term)
	/**
	 * This method should get relevant subtries wie "queries", e.g. query(m_1, new Variable(), return)...
	 */
	// store queries and answer to queries?
	// FIXME Rather than store them, return results immediately
	public List<Term> query(Term... terms) {
		//assert root : "Can query root only";

		List<Term> result = new LinkedList<Term>();
		Trie startTrie = firstChild;
		
		if (startTrie != null) {
			List<Term> dq = new LinkedList<Term>();
			for (Term term : terms) {
				dq.addAll(breakDown(term)); // break down to atoms
			}
			startTrie.query(new TermStack(dq.toArray(new Term[0])), result);
			return result;
		} else {
			return result; // nothing can be found
		}
	}
	
	private void query(TermStack ts, List<Term> result) {
		assert ts.size() > 0 : "Term stack size is 0";
		
		Term first = ts.peek();
		if (same(first, label) || isFreeVar(first)) {
			
			// the last element matched...
			if (ts.size() == 1) {
				if (term != null) {
					// add this term to result...
					result.add(term);
				} else {
					// or add all terms of this trie to result
					collectLeafTerms(this, result);
				}
				// nothing more to process
				return;
			}
			
			// search in subtrie...
			if (firstChild != null) {
				
				// the original term stack may be needed if first is a variable
				TermStack tsMinusOne;
				if (first.isVariable()) {
					tsMinusOne = ts.copy();
				} else {
					tsMinusOne = ts;
				}
				tsMinusOne.pop();
				firstChild.query(tsMinusOne, result);
			}
			
			// and (start) search also in sibling tries if variable
			if (first.isVariable() && nextSibling != null) {
				nextSibling.query(ts.copy(), result);
			}
			
		} else if (nextSibling != null) {
			nextSibling.query(ts, result);
		}
	}

	public List<Term> getAllTerms() {
		assert isRoot() : "Only call to root allowed";
		
		List<Term> result = new ArrayList<Term>();
		collectLeafTerms(this, result);
		return result;
	}
	
	// breaks a term down to atoms and variables
	// TODO Maybe a way without lists
	private List<Term> breakDown(Term term) {
		assert term != null : "Term is null";
		
		List<Term> terms = new LinkedList<Term>();
		
		// add functor as first term (or self if variable)
		if (term.isVariable()) {
			terms.add(term);
		} else {
			terms.add(term.functor());
		}

		// add other terms (recursively)
		if (term.isCompoundTerm()) {
			for (int i = 0; i < term.arity(); i++) {
				terms.addAll(breakDown(term.arg(i)));
			}
		}

		return terms;
	}
	
	/**
	 * Should yield the same results as {@link Term#unify(Term)} but <b>without 
	 * unification</b> and <b>only if the specified terms are either atoms or 
	 * variables</b>.
	 *  
	 * @param t0 The first term.
	 * @param t1 The second term.
	 * @return <tt>true</tt> if so.
	 */
	private boolean same(Term t0, Term t1) {
		if (t0 == null) {
			return t1 == null; // null == null
		} else if (t1 == null) {
			return false; // a1 != null
		} else if (t0.isStringAtom() && t1.isStringAtom()) {
			return t0.asStringAtom().sameAs(t1.asStringAtom());
		} else if (t0.isIntegerAtom() && t1.isIntegerAtom()) {
			return t0.asIntegerAtom().sameAs(t1.asIntegerAtom());
		} else if (t0.isVariable() && t1.isVariable()) {
			// variables...
			Variable v0 = t0.asVariable();
			Variable v1 = t1.asVariable();
			if (!v0.isInstantiated() && !v1.isInstantiated()) {
				// for this purpose two variables are the same, if they are not instantiated (XXX ?)
				return true;
			} else if (v0.isInstantiated() && v1.isInstantiated()) {
				return same(v0.binding(), v1.binding()); // loop of doom with cyclic bindinds?
			}
		}
		
		return false;
	}
	
	private boolean isFreeVar(Term term) {
		return term.isVariable() && !term.asVariable().isInstantiated();
	}

	private void collectLeafTerms(Trie trie, List<Term> result) {
		if (trie.term != null) {
			result.add(trie.term);
		} else {
			Trie child = trie.firstChild;
			while (child != null) {
				collectLeafTerms(child, result);
				child = child.nextSibling;
			}
		}
	}
	
	@Override
	public String toString() {
		String lStr = label != null ? label.toString() : "null";
		String pStr = parent != null && parent.label != null ? parent.label.toString() : "null";
		String fStr = firstChild != null && firstChild.label != null ? firstChild.label.toString() : "null";
		String nStr = nextSibling != null && nextSibling.label != null ? nextSibling.label.toString() : "null";
		return "[Trie " + lStr + ", parent=" + pStr + ", firstChild=" + fStr + ", nextSibling=" + nStr + "]";
	}
	
	public Trie getPredicateSubtrie(StringAtom functor) {
		assert isRoot() : "Can get predicate subtries only from root";

		// precidate functors are direct root children
		Trie child = firstChild;
		while (child != null) {
			if (same(child.label, functor)) {
				break;
			}
			child = child.nextSibling;
		}
		
		assert child != null : "No predicate for specified functor";
		return child;
	}
	
	/**
	 * @see TrieIterator#TrieIterator(Trie, Term...)
	 */
	public Iterator<Term> iterator(Term ... terms) {
		return new TrieIterator(this, terms);
	}
	
	private class TrieIterator implements Iterator<Term> {
		
		private Trie current;
		private Trie hook;
		private Term next;
		private TermStack stack;
		
		/**
		 * Creates a new trie iterator that starts from <tt>start</tt> and 
		 * returns only terms that match the term represented by <tt>terms</tt>.
		 * 
		 * @param start The start trie, e.g., a functor.
		 * @param terms A query represented by an array of terms.
		 */
		public TrieIterator(Trie start, Term ... terms) {
			
			// break down terms to atoms/variables
			List<Term> list = new LinkedList<Term>();
			for (Term term : terms)
				list.addAll(breakDown(term));
			stack = new TermStack(list.toArray(new Term[0]));
			
			current = start;
			next = null;
		}
		
		public boolean hasNext() {
			findNext();
			return next != null;
		}
		
		public Term next() {
			return next;
		}
		
		/**
		 * Finds the next term, i.e., sets {@link TrieIterator#next}.
		 */
		private void findNext() {			
			next = null;
			
			// as long as we haven't found a new next and are not at an end point, i.e., current is null
			while (next == null && current != null) { // or break!
				
				// sophisticated debugging
				System.out.println("current=" + current);
				System.out.println("stack=" + stack);
				
				// get next atom/variable
				Term first = stack.peek();
				
				// stack size may be 0 if we are in leaf retrieval
				if (stack.size() == 0 || same(current.label, first) || isFreeVar(first)) { // match
					
					if (stack.size() == 1 || stack.size() == 0) { // we have the last element or are in leaf retrieval mode
						if (current.term != null) { // if current is a leaf
							
							// set found next
							next = current.term;
							
							// set variables to continue search (parent's sibling or null)...
							if (stack.size() > 0) { // normal mode
								System.out.println("Going back to parent's next sibling");
								current = (current.parent != null) ? current.parent.nextSibling : null;
								stack.back();
							} else { // leaf retrieval mode
								System.out.println("Going back to parent");
								current = current.parent; // parent cannot be null
							}
							
							// a next was found, break
							break;
							
						} else { // current is NOT a leaf
							hook = current; // hook we can jump back to if the leaf term (or all leaf terms!) was retrieved
							
							System.out.println("Entering leaf retrieval mode and continuing with first child");
							
							// advance one step
							current = current.firstChild;
							stack.pop();
						}
					} else if (current.firstChild != null) {
						System.out.println("Continuing with first child");
						stack.pop();
						current = current.firstChild; // continue with first child
					}
					
				} else if (current.nextSibling != null) { // no match
					System.out.println("Continuing with next sibling");
					current = current.nextSibling; // continue with next sibling
				} else { // end point reached
					if (hook != null) {
						System.out.println("Jumping back to hook");
						current = hook; // jump back to hook
						hook = null;
					} else { // no more solutions can be found
						System.out.println("The End.");
						break; // terminate
					}
				}
			}
		}
		
		private Term nextLeaf(Trie start) {
			Trie trie = start;
			Term leaf = null;
			while (leaf == null) { // or break
				leaf = trie.term;
				
			}
			
			return leaf;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}	
	}
}
