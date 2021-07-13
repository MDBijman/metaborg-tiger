package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_remove_node_0_0 extends Strategy {
	
	public static flock_remove_node_0_0 instance = new flock_remove_node_0_0();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm node) {
		Flock.beginTime("api@remove");
		Flock.log("api", "[remove-node] " + node.toString());
		Flock.instance.removeNode(node);
		Flock.endTime("api@remove");
		return node;
    }
}