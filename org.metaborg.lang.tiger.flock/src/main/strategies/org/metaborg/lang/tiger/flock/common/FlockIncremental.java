package org.metaborg.lang.tiger.flock.common;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.impl.GraphFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;

public class FlockIncremental extends Flock {

	public FlockIncremental() {

	}

	@Override
	public void init(IStrategoTerm program) {
		this.clearAnalyses();
		for (Node n : this.graph.nodes()) {
			this.addToNew(n);
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
		this.graph.computeIntervals();
		this.graph.validate();
		initPosition(graph, context.getFactory());
	}

	@Override
	public void replaceNode(IStrategoTerm current, IStrategoTerm replacement) {
		Flock.increment("replaceNode");
		Flock.beginTime("FlockIncremental@replaceNode");

		Flock.beginTime("FlockIncremental@replaceNode:termTree");
		Set<Node> removedNodes = getAllNodes(current);
		{
			Node currentNode = Helpers.getTermNode(current);

			for (Analysis a : this.analyses) {
				this.lowerAnalysisResults(a, removedNodes, currentNode);
			}

			this.termTree.replace(Helpers.getTermId(current), replacement);
		}
		Flock.endTime("FlockIncremental@replaceNode:termTree");

		Flock.beginTime("FlockIncremental@replaceNode:cfg");
		{
			// Patch the graph, removing old nodes and placing new nodes
			Graph subGraph = GraphFactory.createCfgOnce(this.termTree, replacement);
			subGraph.removeGhostNodes();
			subGraph.computeIntervals();
			this.graph.replaceNodes(removedNodes, subGraph);

			for (Node n : subGraph.nodes()) {
				this.addToNew(n);
			}

			this.termTree.validate();

			for (Node n : this.graph.nodes()) {
				if (this.termTree.nodeById(n.getId()) == null) {
					throw new RuntimeException("Null node in cfg");
				}
			}
			this.graph.validate();
		}
		Flock.endTime("FlockIncremental@replaceNode:cfg");
		Flock.endTime("FlockIncremental@replaceNode");
	}

	@Override
	public void removeNode(IStrategoTerm node) {
		Flock.increment("removeNode");
		Flock.beginTime("FlockIncremental@removeNode");
		Flock.log("api", "removing node " + node.toString());

		Node currentNode = Helpers.getTermNode(node);
		Set<Node> removedNodes = getAllNodes(node);

		for (Analysis a : this.analyses) {
			this.lowerAnalysisResults(a, removedNodes, currentNode);
		}

		// Go through graph and remove facts with origin in removed id's
		this.applyGhostMask(removedNodes);
		this.graph.removeGhostNodes();
		Flock.endTime("FlockIncremental@removeNode");
	}

	private void lowerAnalysisResults(Analysis a, Set<Node> removedNodes, Node currentNode) {
		if (a instanceof AnalysisWithDependencies) {
			Set<Node> dependents = new HashSet<>();
			dependents.addAll(removedNodes);
			if (currentNode != null && this.graph.getNode(currentNode.getId()) != null) {
				dependents.addAll(getTermDependencies(this.graph, currentNode));
			}

			for (Node n : dependents) {
				((AnalysisWithDependencies) a).removeFacts(graph, n.getId());
			}
		} else {
			float earliest = 0;
			if (a.direction == Direction.FORWARD) {
				earliest = removedNodes.stream().map(n -> n.interval).min(Float::compareTo).get();
			} else {
				earliest = removedNodes.stream().map(n -> n.interval).max(Float::compareTo).get();
			}

			a.removeResultAfterBoundary(graph, earliest);
		}

		// Remove the replaced nodes in the analysis class
		a.remove(graph, removedNodes);
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
