package org.metaborg.lang.tiger.flock.ast;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_replace_node_impl_0_1 extends Strategy {
	
	public static flock_replace_node_impl_0_1 instance = new flock_replace_node_impl_0_1();
	
	@Override 
	public IStrategoTerm invoke(Context context, IStrategoTerm newNode, IStrategoTerm oldNode) {
        ITermFactory factory = context.getFactory();
        Flock.beginTime("api@replace");
        Flock.log("api", "[replace-node] " + oldNode.toString() + " with " + newNode.toString());
        Flock.instance.replaceNode(oldNode, newNode);
        Flock.endTime("api@replace");
		return newNode;
    }
}