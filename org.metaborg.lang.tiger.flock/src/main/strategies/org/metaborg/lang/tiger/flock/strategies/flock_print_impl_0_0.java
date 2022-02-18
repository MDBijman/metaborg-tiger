package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_print_impl_0_0 extends Strategy {
	
	public static flock_print_impl_0_0 instance = new flock_print_impl_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm node) {
		System.out.println(node.toString());
		return node;
    }
}