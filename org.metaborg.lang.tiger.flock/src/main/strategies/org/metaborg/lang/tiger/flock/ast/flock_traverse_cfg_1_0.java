package org.metaborg.lang.tiger.flock.ast;

import java.util.Stack;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.TermId;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_traverse_cfg_1_0 extends Strategy {

	public static flock_traverse_cfg_1_0 instance = new flock_traverse_cfg_1_0();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm c, Strategy s) {
		s.invoke(context, c);
		/*
		 * How do we traverse the cfg and apply optimizations?
		 * Optimizations may change the cfg, don't want the rug pulled out from under us
		 * 
		 * We can do preorder traversal over the program and apply s if t is a cfg node
		 * Since optimizations will only ever change t and/or its children
		 * 
		 * This strategy succeeds if any applications of the given strategy succeed
		 */

		boolean hasSucceeded = false;

		TermId id = Helpers.getTermId(c);
		ITerm root = Flock.instance.termTree.nodeById(id);

		// We must handle the root separately since it will become the return value of
		// this strategy, so we need to capture its output

		TermId rootId = id;
		if (Flock.instance.graph.getNode(root.getId()) != null) {
			IStrategoTerm result = s.invoke(context, c);
			rootId = Helpers.getTermId(result);
			root = Flock.instance.termTree.nodeById(rootId);
			hasSucceeded |= (result != null);
		}

		Stack<ITerm> terms = new Stack<>();
		// Reverse push onto stack
		for (int i = root.childrenCount() - 1; i >= 0; i--) {
			terms.push(root.childAt(i));
		}

		while (!terms.isEmpty()) {
			ITerm next = terms.pop();

			if (Flock.instance.graph.getNode(next.getId()) != null) {
				IStrategoTerm result = s.invoke(context, next.toTerm());
				
				if (result != null) {
					next = Flock.instance.termTree.nodeById(Helpers.getTermId(result));
					hasSucceeded = true;
				}
			}

			// Reverse push onto stack
			for (int i = next.childrenCount() - 1; i >= 0; i--) {
				terms.push(next.childAt(i));
			}
		}
		
		return hasSucceeded ? Flock.instance.termTree.nodeById(rootId).toTerm() : null;
	}
}