package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_replace_node_impl_0_1 extends Strategy {
	
	public static flock_replace_node_impl_0_1 instance = new flock_replace_node_impl_0_1();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm newNode, IStrategoTerm oldNode) {
		Flock.log("api", "[replace-node] " + oldNode.toString() + " with " + newNode.toString());
        Flock.beginTime("Helpers@validateIds");
		Helpers.validateIds(newNode);
        Helpers.validateIds(oldNode);
        Flock.endTime("Helpers@validateIds");
        Flock.instance.replaceNode(oldNode, newNode);
		return newNode;
    }
}