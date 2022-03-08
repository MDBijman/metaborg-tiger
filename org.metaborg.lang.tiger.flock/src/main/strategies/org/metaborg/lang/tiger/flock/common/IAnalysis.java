package org.metaborg.lang.tiger.flock.common;

import java.util.Set;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.NotImplementedException;

public interface IAnalysis {
	public enum Direction {
		FORWARD, BACKWARD
	}

	public void addToNew(SCCs sccs, Node n);

	public void addToDirty(Component c);

	public void addToClean(Component c);

	public String getName();

	public void remove(Component c);

	public Direction getDirection();

	public void removeAnalysisResults(SCCs sccs, Component c);

	public void validate(Graph graph, SCCs graph_scss);

	public default void performDataAnalysis(Graph graph, SCCs graph_scss, Node node) {
		throw new NotImplementedException();
	}

	public default IStrategoTerm performDataAnalysis(Graph graph, SCCs graph_scss, Node node, IStrategoTerm specialization) {
		throw new NotImplementedException();
	}
}
