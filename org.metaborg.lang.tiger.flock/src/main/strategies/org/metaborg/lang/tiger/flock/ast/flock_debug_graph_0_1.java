package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_debug_graph_0_1 extends Strategy {
	
	public static flock_debug_graph_0_1 instance = new flock_debug_graph_0_1();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm program, IStrategoTerm propertyName) {
		Flock.printDebug(propertyName.toString());
		IStrategoString name = (IStrategoString) propertyName;
		Flock.log("debug", Flock.instance.graph.toGraphviz(name.stringValue()).replace("\n", "\t"));
        return program;
    }
}