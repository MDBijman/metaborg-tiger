package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_disable_logging_0_0 extends Strategy {
	
	public static flock_disable_logging_0_0 instance = new flock_disable_logging_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm program) {
		Flock.disableLogs();
		return program;
    }
}