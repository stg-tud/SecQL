package saere.database.index.simple;

import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.InsertStack;
import saere.database.index.Matcher;
import saere.database.index.QueryStack;
import saere.database.index.ShallowTermFlattener;

/**
 * The {@link SimpleTrieBuilder} uses for each element of a flattend term 
 * representation exactly one {@link SimpleTrie} node. That is, if the length of a 
 * flattened term repesenation is seven, the term will be found at node level 
 * seven (not counting the root).<br/>
 * <br/>
 * For example, the representation <code>[f, along, b, c]</code> will be 
 * stored/retrieved with four nodes with the labels <tt>f</tt>, <tt>along</tt>, 
 * <tt>b</tt> and <tt>c</tt> whereas the last node with label <tt>c</tt> at 
 * level four stores the term.
 * 
 * @author David Sullivan
 * @version 0.31, 10/17/2010
 */
public final class SimpleTrieBuilder {
	
	private static final ShallowTermFlattener flattener = new ShallowTermFlattener();
	
	private SimpleTrie current;
		
	public SimpleTrie insert(Term term, SimpleTrie start) {
		InsertStack stack = new InsertStack(flattener.flattenForInsertion(term));
		current = start;
		
		SimpleTrie insertionNode = null; // the trie node where the specified term will be added
		while (insertionNode == null) {
			Atom first = stack.peek();
			
			if (current.parent == null) { 
				
				if (current.firstChild == null) // create the very first node
					current.firstChild = new SimpleTrie(first, current);
				current = current.firstChild;
				
			} else if (Matcher.match(current.label, first)) { // the labels match
				
				// remove the first atom/variable from stack
				stack.pop();
				
				// this must be the insertion node (and it already existed)
				if (stack.size() == 0) {
					
					current.addTerm(term);
					insertionNode = current;
				
				} else { // more to process
					
					if (current.firstChild == null) // add to own subtrie
						current.firstChild = new SimpleTrie(stack.peek(), current);
					current = current.firstChild;
				}

			} else { // !root && !same
				
				if (current.nextSibling == null) // add to (a) sibling subtrie
					current.nextSibling = new SimpleTrie(stack.peek(), current.parent);
				current = current.nextSibling;
			}
		}
		
		return insertionNode;
	}

	public boolean remove(Term term, SimpleTrie start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	
	public Iterator<Term> iterator(SimpleTrie start) {
		return new SimpleTermIterator(start);
	}
	
	public Iterator<Term> iterator(SimpleTrie start, Term[] query) {
		return new SimpleQueryIterator(start, new QueryStack(flattener.flattenForQuery(query)));
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-simple";
	}
}