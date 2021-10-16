package org.metaborg.lang.tiger.flock.ast;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockLattice;

import org.spoofax.terms.ParseError;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoInt;

public class flock_update_term_map_0_0 extends Strategy {
	
	public static flock_update_term_map_0_0 instance = new flock_update_term_map_0_0();
		
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm node) {
        //Flock.instance.setNodeTerms(node);
        return node; 
    }
}

