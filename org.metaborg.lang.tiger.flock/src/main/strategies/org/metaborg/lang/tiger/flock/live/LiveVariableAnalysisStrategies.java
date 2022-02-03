package org.metaborg.lang.tiger.flock.live;

import java.util.Collection;
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

public class LiveVariableAnalysisStrategies {
	public static class get_live_0_0 extends Strategy {
		public static get_live_0_0 instance = new get_live_0_0();

		@Override
		public IStrategoTerm invoke(Context context, IStrategoTerm current) {
			ITermFactory factory = context.getFactory();
			TermId id = new TermId(((IStrategoInt) current).intValue());
			Node node = Flock.instance.getNode(id);
			if (node == null) {
				Flock.printDebug("CfgNode is null with id " + id.getId());
				return null;
			}
			Flock.instance.analysisWithName("live").performDataAnalysis(Flock.instance.graph, Flock.instance.graph_scss,
					node);
			IStrategoList result = factory
					.makeList(((Collection<? extends IStrategoTerm>) node.getProperty("live").lattice.value()).stream()
							.map(n -> Helpers.toTerm(n)).collect(Collectors.toList()));
			return result;
		}
	}
}