package org.metaborg.lang.tiger.flock.common;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.impl.GraphFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;

public class FlockIterative extends Flock {
	
	public FlockIterative() {
		
	}

	@Override
	public void init(IStrategoTerm program) {
		this.clearAnalyses();
		for (Node n : this.graph.nodes()) {
			this.addToNew(n);
		}
		update(program);
	}

	@Override
	public void update(IStrategoTerm program) {
		Flock.beginTime("Program@update");
		for (Node node : this.graph.nodes()) {
			node.interval = this.graph.intervalOf(node);
		}
		setNodeTerms(program);
		Flock.endTime("Program@update");
	}
	
	@Override
	public void createControlFlowGraph(Context context, IStrategoTerm current) {
		this.io = context.getIOAgent();
		this.graph = GraphFactory.createCfgRecursive(current);
		Flock.printDebug(this.graph.toGraphviz());
		this.graph.removeGhostNodes();
		this.graph.computeIntervals();
		this.graph.validate();
		this.update(current);
		initPosition(graph, context.getFactory());
	}

	@Override
	public void replaceNode(IStrategoTerm current, IStrategoTerm replacement) {
		Flock.increment("replaceNode");
		
		Flock.beginTime("Program@replaceNode");
		Flock.beginTime("Program@replaceNode - a");

		// Nodes outside of the removed id's that have analysis results directly
		// depending on them (static analysis)
		Set<Node> dependents = new HashSet<>();

		Node currentNode = Helpers.getTermNode(current);
		if (currentNode != null && this.graph.getNode(currentNode.getId()) != null) {
			dependents.addAll(getTermDependencies(this.graph, currentNode));
		}

		// Set of id's that were in the removed node
		Set<Node> removedNodes = getAllNodes(current);
		removedNodes = removedNodes.stream()
				.filter(n -> this.graph.getNode(n.getId()) != null)
				.collect(Collectors.toSet());

		// Add removed id's to that set
		dependents.addAll(removedNodes);
		
		// Go through graph and remove facts with origin in removed id's
		this.applyGhostMask(removedNodes);
		this.removeAnalysisFacts(dependents);
		this.removeFromAnalysis(removedNodes);
		Flock.endTime("Program@replaceNode - a");

		// Here we patch the graph
		Flock.beginTime("Program@replaceNode - b");
		Graph subGraph = GraphFactory.createCfgRecursive(replacement);
		subGraph.removeGhostNodes();
		subGraph.computeIntervals();
		this.graph.replaceNodes(removedNodes, subGraph);

		for (Node n : subGraph.nodes()) {
			this.addToNew(n);
		}

		// this.allNodes.addAll(newNodes);
		Flock.endTime("Program@replaceNode - b");
		Flock.endTime("Program@replaceNode");
	}

	@Override
	public void removeNode(IStrategoTerm node) {
		Flock.increment("removeNode");
		Flock.beginTime("Program@removeNode");
		Flock.log("api", "removing node " + node.toString());
		// Nodes outside of the removed id's that have analysis results directly
		// depending on them (static analysis)
		Set<Node> dependents = new HashSet<>();

		Node currentNode = Helpers.getTermNode(node);
		if (currentNode != null && this.graph.getNode(currentNode.getId()) != null) {
			dependents.addAll(getTermDependencies(this.graph, currentNode));
		}

		// Set of id's that were in the removed node
		Set<Node> removedNodes = getAllNodes(node);

		this.removeFromAnalysis(removedNodes);

		// Add removed id's to that set
		dependents.addAll(removedNodes);

		// Go through graph and remove facts with origin in removed id's
		this.applyGhostMask(removedNodes);
		this.removeAnalysisFacts(dependents);
		this.graph.removeGhostNodes();
		Flock.endTime("Program@removeNode");
	}
	
	@Override
	public Node getNode(CfgNodeId id) {
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
	
	private void removeAnalysisFacts(Set<Node> toRemove) {
		for (Analysis a : analyses) {
			for (Node id : toRemove) {
				a.removeFacts(graph, id.getId());
			}
		}
	}
}
