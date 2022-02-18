package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.TermTree;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_debug_termgraph_0_0 extends Strategy {
	
	public static flock_debug_termgraph_0_0 instance = new flock_debug_termgraph_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm program) {
		Flock.logTimers();
		Flock.logCounts();
		Flock.instance.termTree.validate();
		String result = Flock.instance.termTree.toString();
		Flock.log("debug", result);
		return context.getFactory().makeString(result);
    }
}