package org.metaborg.lang.tiger.flock.common;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.metaborg.lang.tiger.flock.impl.GraphFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;

public class FlockIncremental extends Flock {

	public FlockIncremental() {

	}

	private void validate() {
		this.termTree.validate();
		this.graph.validate();
		for (Analysis a : this.analyses) {
			a.validate(this.graph, this.graph_scss);
		}
	}

	@Override
	public void init(IStrategoTerm program) {
		this.clearAnalyses();
		for (Node n : this.graph.nodes()) {
			this.addToNew(n);
		}
		for (Component c : this.graph_scss.components) {
			this.addToDirty(c);
		}
	}

	@Override
	public void createTermGraph(IStrategoTerm term) {
		this.termTree = new TermTree(term);
	}

	@Override
	public void createControlFlowGraph(Context context, IStrategoTerm current) {
		this.io = context.getIOAgent();
		this.graph = GraphFactory.createCfgRecursive(this.termTree, current);
		this.graph.removeGhostNodes();
		this.graph.validate();
		initPosition(graph, context.getFactory());
		this.graph_scss = new SCCs(this.graph);
	}

	@Override
	public void replaceNode(IStrategoTerm current, IStrategoTerm replacement) {
		try {
			Flock.increment("replaceNode");
			Flock.beginTime("FlockIncremental@replaceNode");
			this.validate();
			Flock.beginTime("FlockIncremental@replaceNode:termTree");
			Set<Node> removedNodes = getAllNodes(current);
			{
				this.termTree.replace(Helpers.getTermId(current), replacement);
				this.termTree.validate();
			}
			Flock.endTime("FlockIncremental@replaceNode:termTree");
			{
				for (Analysis a : this.analyses) {
					this.removeAnalysisResultsAfter(a, removedNodes);
				}
			}
			Flock.beginTime("FlockIncremental@replaceNode:cfg");
			{
				// Patch the graph, removing old nodes and placing new nodes
				Graph subGraph = GraphFactory.createCfgOnce(this.termTree, replacement);
				subGraph.removeGhostNodes();

				Set<Node> predecessors = new HashSet<>();
				Set<Node> successors = new HashSet<>();
				for (Node n : removedNodes) {
					predecessors.addAll(this.graph.parentsOf(n));
					successors.addAll(this.graph.childrenOf(n));
				}
				predecessors.removeAll(removedNodes);
				successors.removeAll(removedNodes);

				this.graph.replaceNodes(removedNodes, predecessors, successors, subGraph);
				this.graph_scss.replaceNodes(this.graph, removedNodes, predecessors, successors, subGraph);
				this.graph_scss.validate(this.graph);

				// This is some useful validation logic when looking for bugs in SCC creation

				// SCCs new_scss = new SCCs(this.graph);
				// if (new_scss.components.size() != this.graph_scss.components.size()) {
				// throw new RuntimeException("Inc. vs from-scratch SCCs don't match");
				// }

				for (Node n : subGraph.nodes()) {
					this.addToNew(n);
				}
			}

			Flock.endTime("FlockIncremental@replaceNode:cfg");
			this.validate();
			Flock.endTime("FlockIncremental@replaceNode");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void removeNode(IStrategoTerm node) {
		Flock.increment("removeNode");
		Flock.beginTime("FlockIncremental@removeNode");
		Flock.log("api", "removing node " + node.toString());

		Set<Node> removedNodes = getAllNodes(node);

		for (Analysis a : this.analyses) {
			this.removeAnalysisResultsAfter(a, removedNodes);
		}

		// Go through graph and remove facts with origin in removed id's
		this.applyGhostMask(removedNodes);
		this.graph.removeGhostNodes();
		Flock.endTime("FlockIncremental@removeNode");
	}

	private void removeAnalysisResultsAfter(Analysis a, Set<Node> removedNodes) {
		Set<Component> outdatedComponents = removedNodes.stream().map(this.graph_scss.nodeComponent::get)
				.collect(Collectors.toSet());
		Set<Component> allOutdatedComponents = new HashSet<>(outdatedComponents);

		if (a.direction == Direction.FORWARD) {
			for (Component c : outdatedComponents) {
				this.collectSuccessors(c, allOutdatedComponents);
			}
		} else {
			for (Component c : outdatedComponents) {
				this.collectPredecessors(c, allOutdatedComponents);
			}		
		}
		
		for (Component c : allOutdatedComponents) {
			this.removeAnalysisResultsIn(a, c);
		}

		for (Node n : removedNodes) {
			a.remove(n);
		}
	}

	private void removeAnalysisResultsIn(Analysis a, Component c) {
		a.removeNodeResults(this.graph_scss, c.nodes);
	}

	private void collectPredecessors(Component c, Set<Component> result) {
		for (Component neighbour : this.graph_scss.revNeighbours.get(c)) {
			if (!result.contains(neighbour)) {
				result.add(neighbour);
				collectPredecessors(neighbour, result);
			}
		}
	}

	private void collectSuccessors(Component c, Set<Component> result) {
		for (Component neighbour : this.graph_scss.neighbours.get(c)) {
			if (!result.contains(neighbour)) {
				result.add(neighbour);
				collectSuccessors(neighbour, result);
			}
		}
	}

	@Override
	public Node getNode(TermId id) {
		return graph.getNode(id);
	}

	@Override
	public Analysis analysisWithName(String name) {
		for (Analysis a : analyses) {
			if (a.name.equals(name)) {
				return a;
			}
		}
		throw new RuntimeException("No analysis with name " + name);
	}

}
