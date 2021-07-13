package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_update_0_0 extends Strategy {
	
	public static flock_update_0_0 instance = new flock_update_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm program) {
        ITermFactory factory = context.getFactory();
        
        Flock.beginTime("api@update");
        
        Flock.log("api", "[update] " + program.toString());
        if (Flock.isLogEnabled("graphviz"))
        	Flock.log("graphviz", "at [update] " + Flock.instance.graph.toGraphviz().replace("\n", "\t"));
        Flock.instance.update(program);

		Flock.endTime("api@update");
		
        return program;
    }
}