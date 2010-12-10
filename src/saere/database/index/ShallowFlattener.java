package saere.database.index;

import saere.CompoundTerm;
import saere.Term;

/**
 * The {@link ShallowFlattener} flattens terms using their functor and 
 * arguments. If the argument is a {@link CompoundTerm} its functor is 
 * used.<br>
 * <br>
 * For example the terms <code>f(a, b, c)</code> and <code>f(a, b(c))</code> 
 * are flattend to <code>[f, a, b, c]</code> and <code>[f, a, b]</code>, 
 * respectively.<br>
 * <br>
 * Flattened term representations created by the {@link ShallowFlattener} 
 * tend to be shorter than the ones created with the 
 * {@link FullFlattener}.
 * 
 * @author David Sullivan
 * @version 0.3, 11/9/2010
 */
public final class ShallowFlattener extends TermFlattener {
	
	@Override
	public LabelStack flatten(Term term) {
		Label[] labels;
		
		// We assume that we get mostly compound terms
		if (term.isCompoundTerm()) {
			labels = new Label[term.arity() + 1];
			labels[0] = FunctorLabel.FunctorLabel(term.functor(), term.arity());
			
			// Reorder according to profilings here (if we use profilings)!
			Term[] args = getArgs(term);
			
			for (int i = 0; i < args.length; i++) {
				labels[i + 1] = flattenArg(args[i]);
			}
		} else if (term.isIntegerAtom()) {
			labels = new Label[] { AtomLabel.AtomLabel(term.asIntegerAtom()) };
		} else if (term.isStringAtom()) {
			labels = new Label[] { AtomLabel.AtomLabel(term.asStringAtom()) };
		} else if (term.isVariable()) {
			Term binding = term.asVariable().binding();
			if (binding != null) {
				return flatten(binding);
			} else {
				labels = new Label[] { VariableLabel.VariableLabel() };
			}
		} else {
			// Should suffice for lists
			labels = new Label[] { FunctorLabel.FunctorLabel(term.functor(), 1) };
		}
		
		return new LabelStack(labels);
	}
	
	private Label flattenArg(Term term) {
		if (term.isStringAtom()) {
			return AtomLabel.AtomLabel(term.functor());
		} else if (term.isCompoundTerm()) {
			return FunctorLabel.FunctorLabel(term.functor(), term.arity());
		} else if (term.isIntegerAtom()) {
			return AtomLabel.AtomLabel(term.asIntegerAtom());
		} else if (term.isVariable()) {
			Term binding = term.asVariable().binding();
			if (binding != null) {
				return flattenArg(binding);
			} else {
				return VariableLabel.VariableLabel();
			}
		} else { // term.isList()
			if (term.arity() == 0) {
				return AtomLabel.AtomLabel(term.functor()); // Empty list
			} else {
				return FunctorLabel.FunctorLabel(term.functor(), term.arity());
			}
		}
	}

	@Override
	public String toString() {
		return "shallow";
	}
}