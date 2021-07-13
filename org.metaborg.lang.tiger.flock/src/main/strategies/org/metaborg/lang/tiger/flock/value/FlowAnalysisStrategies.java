package org.metaborg.lang.tiger.flock.value;

import java.util.HashMap;

import org.metaborg.lang.tiger.flock.common.CfgNodeId;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.value.FlowAnalysisProperties.ConstProp;
import org.spoofax.interpreter.terms.IStrategoInt;
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
			CfgNodeId id = new CfgNodeId(((IStrategoInt) current).intValue());
			Node node = Flock.instance.getNode(id);
			if (node == null) {
				Flock.printDebug("CfgNode is null with id " + id.getId());
				return current;
			}
			Flock.instance.analysisWithName("values").updateUntilBoundary(Flock.instance.graph, node);
			IStrategoTerm result = Helpers.toTerm(((HashMap<IStrategoTerm, Value>) node.getProperty("values").lattice.value()));
			return result;
		}
	}
}