package org.metaborg.lang.tiger.flock.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.TermId;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoInt;

public class flock_initialize_impl_0_0 extends Strategy {
	
	public static flock_initialize_impl_0_0 instance = new flock_initialize_impl_0_0();
		
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        ITermFactory factory = context.getFactory();
		
		try {
			Flock.resetTimers();
			Flock.resetCounters();
			Flock.beginTime("api@initialize");
			Flock.instance.io = context.getIOAgent();
			Flock.instance.factory = factory;
			Flock.log("api", "initialize");

			Flock.instance.createTermGraph(current);
			Flock.instance.createControlFlowGraph(context, current);
			//Flock.log("graphviz", Flock.instance.graph.toGraphviz());
			Flock.instance.init(current);
			Flock.endTime("api@initialize");
		} catch (ParseError e) {
			context.getIOAgent().printError(e.toString());
			return null;
		}
		
        return current; 
    }
}

