package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_aterm.pp_aterm_0_0;
import org.strategoxt.stratego_lib.debug_0_0;
import org.strategoxt.stratego_lib.strip_annos_0_0;

public class flock_replace_node_impl_0_1 extends Strategy {

	public static flock_replace_node_impl_0_1 instance = new flock_replace_node_impl_0_1();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm newNode, IStrategoTerm oldNode) {
		if (Flock.isLogEnabled("api")) {
			//debug_0_0.instance.invoke(context, pp_aterm_0_0.instance.invoke(context, strip_annos_0_0.instance.invoke(context, oldNode)));
			Flock.log("api", "[replace-node] " + oldNode.toString(3) + " with " + newNode.toString(3));
		}
		Flock.beginTime("Helpers@validateIds");
		Helpers.validateIds(newNode);
		Helpers.validateIds(oldNode);
		Flock.endTime("Helpers@validateIds");
		Flock.instance.replaceNode(oldNode, newNode);
		return newNode;
	}
}