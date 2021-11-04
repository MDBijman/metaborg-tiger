package org.metaborg.lang.tiger.flock.value;

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
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class FlowAnalysisStrategies {
	public static class get_values_0_0 extends Strategy {
		public static get_values_0_0 instance = new get_values_0_0();

		@Override
		public IStrategoTerm invoke(Context context, IStrategoTerm current) {
			ITermFactory factory = context.getFactory();
			TermId id = new TermId(((IStrategoInt) current).intValue());
			Node node = Flock.instance.getNode(id);
			if (node == null) {
				// Flock.printDebug("CfgNode is null with id " + id.getId());
				return null;
			}
			Flock.instance.analysisWithName("values").updateResultUntilBoundary(Flock.instance.graph, node);
			IStrategoList result = factory.makeList(
					((Map<IStrategoTerm, IStrategoTerm>) node.getProperty("values").lattice.value()).entrySet().stream()
							.map(n -> factory.makeTuple(Helpers.toTerm(n.getKey()), Helpers.toTerm(n.getValue())))
							.collect(Collectors.toList()));
			return result;
		}
	}

	public static class get_value_0_1 extends Strategy {
		public static get_value_0_1 instance = new get_value_0_1();

		@Override
		public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
			ITermFactory factory = context.getFactory();
			TermId id = new TermId(((IStrategoInt) current).intValue());
			Node node = Flock.instance.getNode(id);
			if (node == null) {
				// Flock.printDebug("CfgNode is null with id " + id.getId());
				return null;
			}

			Flock.instance.analysisWithName("values").updateResultUntilBoundary(Flock.instance.graph, node);

			Map<IStrategoTerm, Object> values = (Map<IStrategoTerm, Object>) node
					.getProperty("values").lattice.value();

			Object value = values.get(key);

			if (value == null) {
				return null;
			}

			IStrategoTerm asTerm = Helpers.toTerm(value);

			return asTerm;
		}
	}
}