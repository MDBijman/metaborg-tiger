package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_enable_incremental_0_1 extends Strategy {
	
	public static flock_enable_incremental_0_1 instance = new flock_enable_incremental_0_1();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm program) {
		return program;
    }
}