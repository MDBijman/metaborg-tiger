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

public class flock_analyse_program_0_0 extends Strategy {
	
	public static flock_analyse_program_0_0 instance = new flock_analyse_program_0_0();
		
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        ITermFactory factory = context.getFactory();
		
		try {
			context.getIOAgent().printError("Start Analysis");
			context.getIOAgent().printError("Creating CFG");
			Flock.resetTimers();
			Flock.beginTime("api@analyse");
			Flock.instance.createControlFlowGraph(context, current);
			Flock.log("graphviz", Flock.instance.graph.toGraphviz());
			Flock.log("api", "analyse_program");
			Flock.instance.init(current);
			Flock.endTime("api@analyse");

			
		} catch (ParseError e) {
			context.getIOAgent().printError(e.toString());
			return null;
		}
		
        return current; 
    }
}

