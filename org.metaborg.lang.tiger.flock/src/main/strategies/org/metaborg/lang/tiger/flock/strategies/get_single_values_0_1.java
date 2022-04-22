package org.metaborg.lang.tiger.flock.strategies;

import java.util.Map;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.TermId;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_single_values_0_1 extends Strategy {
	public static get_single_values_0_1 instance = new get_single_values_0_1();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
		ITermFactory factory = context.getFactory();
		TermId id = new TermId(((IStrategoInt) current).intValue());
		Node node = Flock.instance.getNode(id);
		if (node == null) {
			Flock.printDebug("CfgNode is null with id " + id.getId());
			return null;
		}

		IStrategoTerm result = Flock.instance.analysisWithName("values").performDataAnalysis(Flock.instance.graph,
				Flock.instance.graph_scss, node, key);
		return result;
	}
}