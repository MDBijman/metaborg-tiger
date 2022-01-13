package org.metaborg.lang.tiger.flock.ae;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.TermId;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class AvailableExpressionsStrategies {
	public static class get_expressions_0_0 extends Strategy {
		public static get_expressions_0_0 instance = new get_expressions_0_0();

		@Override
		public IStrategoTerm invoke(Context context, IStrategoTerm current) {
			ITermFactory factory = context.getFactory();
			TermId id = new TermId(((IStrategoInt) current).intValue());
			Node node = Flock.instance.getNode(id);
			if (node == null) {
				//Flock.printDebug("CfgNode is null with id " + id.getId());
				return current;
			}
			Flock.instance.analysisWithName("expressions").performDataAnalysis(Flock.instance.graph, Flock.instance.graph_scss, node);
			IStrategoList result = factory
					.makeList(((Map<IStrategoTerm, IStrategoTerm>) node.getProperty("expressions").lattice.value())
							.entrySet().stream().map(e -> factory.makeList(Helpers.toTerm(e.getKey()), Helpers.toTerm(e.getValue())))
							.collect(Collectors.toList()));
			return result;
		}
	}
}