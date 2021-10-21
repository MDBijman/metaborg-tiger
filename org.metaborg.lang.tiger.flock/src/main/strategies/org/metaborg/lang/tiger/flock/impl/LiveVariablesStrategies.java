package org.metaborg.lang.tiger.flock.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.metaborg.lang.tiger.flock.common.TermId;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockCollectionLattice;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.FlockValue.Name;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class LiveVariablesStrategies {
	public static class get_live_0_0 extends Strategy {
		public static get_live_0_0 instance = new get_live_0_0();

		@Override
		public IStrategoTerm invoke(Context context, IStrategoTerm current) {
			ITermFactory factory = context.getFactory();
			TermId id = new TermId(((IStrategoInt) current).intValue());
			Node node = Flock.instance.getNode(id);
			if (node == null) {
				Flock.printDebug("CfgNode is null with id " + id.getId());
				return current;
			}
			Flock.instance.analysisWithName("live").updateResultUntilBoundary(Flock.instance.graph, node);
			IStrategoList result = factory.makeList(((Collection<? extends IStrategoTerm>) node.getProperty("live").lattice.value())
					.stream()
					.map(n -> Helpers.toTerm(n))
					.collect(Collectors.toList()));
			return result;
		}
	}
}