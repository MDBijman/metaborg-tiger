package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_debug_runtime_0_0 extends Strategy {
	
	public static flock_debug_runtime_0_0 instance = new flock_debug_runtime_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm program) {
		Flock.logTimers();
		Flock.logCounts();
		return program;
    }
}