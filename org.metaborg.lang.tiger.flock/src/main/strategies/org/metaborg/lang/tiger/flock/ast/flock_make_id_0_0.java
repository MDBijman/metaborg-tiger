package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_make_id_0_0 extends Strategy {
	
	public static flock_make_id_0_0 instance = new flock_make_id_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm c) {
        ITermFactory factory = context.getFactory();
        int id = (int) Flock.nextNodeId().getId();
		return factory.makeInt(id);
    }
}