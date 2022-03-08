package org.metaborg.lang.tiger.flock.common;

import java.util.HashMap;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.NotImplementedException;

public abstract class SpecializableAnalysis implements IAnalysis {
	String name;
	Direction direction;
	HashMap<IStrategoTerm, SingleAnalysis> specializations;

	public SpecializableAnalysis(String n, Direction d) {
		this.name = n;
		this.direction = d;
		this.specializations = new HashMap<>();
	}

	@Override
	public void addToNew(SCCs sccs, Node n) {
		for (SingleAnalysis sa : specializations.values()) {
			sa.addToNew(sccs, n);
		}
	}

	@Override
	public void addToDirty(Component c) {
		for (SingleAnalysis sa : specializations.values()) {
			sa.addToDirty(c);
		}
	}

	@Override
	public void addToClean(Component c) {
		for (SingleAnalysis sa : specializations.values()) {
			sa.addToClean(c);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void remove(Component c) {
		for (SingleAnalysis sa : specializations.values()) {
			sa.remove(c);
		}
	}

	@Override
	public Direction getDirection() {
		return this.direction;
	}

	@Override
	public void removeAnalysisResults(SCCs sccs, Component c) {
		for (SingleAnalysis sa : specializations.values()) {
			sa.removeAnalysisResults(sccs, c);
		}
	}

	@Override
	public void validate(Graph graph, SCCs graph_scss) {
		for (SingleAnalysis sa : specializations.values()) {
			sa.validate(graph, graph_scss);
		}
	}

	public abstract SingleAnalysis makeSpecialization(IStrategoTerm sub);

	public IStrategoTerm performDataAnalysis(Graph graph, SCCs graph_scss, Node node, IStrategoTerm specialization) {
		SingleAnalysis e = this.specializations.get(specialization);

		Flock.beginTime("SpecializableAnalysis@performDataAnalysis:make");
		if (e == null) {
			e = this.makeSpecialization(specialization);
			for (Node n : graph.nodes()) {
				e.addToNew(graph_scss, n);
			}

			if (this.specializations.size() > 3) {
				this.specializations.remove(this.specializations.keySet().iterator().next());
			}

			this.specializations.put(specialization, e);
		}
		Flock.endTime("SpecializableAnalysis@performDataAnalysis:make");

		Flock.beginTime("SpecializableAnalysis@performDataAnalysis:run");
		e.performDataAnalysis(graph, graph_scss, node);
		Flock.endTime("SpecializableAnalysis@performDataAnalysis:run");

		return Helpers.toTerm(node.getProperty(e.propertyName).lattice.value());
	}
}
