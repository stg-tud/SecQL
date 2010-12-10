package saere.database.index;

import static org.junit.Assert.assertTrue;
import static saere.database.DatabaseTest.same;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

import saere.Term;
import saere.database.OtherTestFacts;
import saere.database.index.FullFlattener;
import saere.database.index.Root;
import saere.database.index.ShallowFlattener;
import saere.database.index.DefaultTrieBuilder;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

/**
 * Minimal test cases for term removal.
 * 
 * @author David Sullivan
 * @version 0.1, 12/6/2010
 */
// TODO Some more test cases / removals
public final class TermRemovalTest {

	private static final int MAP_THRESHOLD = 120;
	
	@Test
	public void testShallowRemoval() {
		TrieBuilder builder = new DefaultTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD);
		Trie root = new Root();
		
		Term[] facts = new Term[OtherTestFacts.ALL_TERMS.length];
		System.arraycopy(OtherTestFacts.ALL_TERMS, 0, facts, 0, facts.length);
		ArrayList<Term> actuals = new ArrayList<Term>();
		
		for (Term fact : facts) {
			builder.insert(fact, root);
			actuals.add(fact);
		}
		
		// Remove term 2
		builder.remove(facts[1], root);
		actuals.remove(1);
		
		assertTrue(same(actuals, expecteds(root, builder)));
	}
	
	@Test
	public void testFullRemoval() {
		TrieBuilder builder = new DefaultTrieBuilder(new FullFlattener(), MAP_THRESHOLD);
		Trie root = new Root();
		
		Term[] facts = new Term[OtherTestFacts.ALL_TERMS.length];
		System.arraycopy(OtherTestFacts.ALL_TERMS, 0, facts, 0, facts.length);
		ArrayList<Term> actuals = new ArrayList<Term>();
		
		for (Term fact : facts) {
			builder.insert(fact, root);
			actuals.add(fact);
		}
		
		// Remove term 2
		builder.remove(facts[1], root);
		actuals.remove(1);
		
		assertTrue(same(actuals, expecteds(root, builder)));
	}
	
	private ArrayList<Term> expecteds(Trie trie, TrieBuilder builder) {
		ArrayList<Term> expecteds = new ArrayList<Term>();
		Iterator<Term> iter = builder.iterator(trie);
		while (iter.hasNext()) {
			expecteds.add(iter.next());
		}
		return expecteds;
	}
}