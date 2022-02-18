package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.metaborg.lang.tiger.flock.graph.GraphFactory;
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
		this.graph = GraphFactory.createCfgRecursive(this.termTree, current);
		this.graph.removeGhostNodes();
		this.graph.validate();
		initPosition(graph, context.getFactory());
		this.graph_scss = new SCCs(this.graph);
	}

	@Override
	public void replaceNode(IStrategoTerm current, IStrategoTerm replacement) {
		Flock.increment("replaceNode");
		Flock.beginTime("FlockIncremental@replaceNode");
		this.validate();
		Flock.beginTime("FlockIncremental@replaceNode:termTree");
		try {
		Set<Node> removedNodes = getAllNodes(current);
		boolean changedIrregular = false;
		{
			changedIrregular |= this.termTree.containsIrregularTerm(Helpers.getTermId(current));
			this.termTree.replace(Helpers.getTermId(current), replacement);
			this.termTree.validate();
		}
		Flock.endTime("FlockIncremental@replaceNode:termTree");
		Flock.beginTime("FlockIncremental@removeAnalysisResults");
		{
			/*
			 * We remove the results that may depend on the removed nodes. We must do this
			 * again if we do an irregular update, since there may be new dependencies from
			 * other nodes onto the new subgraph.
			 */
			for (Analysis a : this.analyses) {
				this.removeAnalysisResultsAfter(a, removedNodes);
			}
		}
		Flock.endTime("FlockIncremental@removeAnalysisResults");
		Flock.beginTime("FlockIncremental@replaceNode:cfg");
		{
			// Patch the graph, removing old nodes and placing new nodes
			Flock.beginTime("FlockIncremental@replaceNode:cfg:create");
			Graph subGraph = GraphFactory.createCfgOnce(this.termTree, replacement);
			subGraph.removeGhostNodes();
			changedIrregular |= this.termTree.containsIrregularTerm(Helpers.getTermId(replacement));
			Flock.endTime("FlockIncremental@replaceNode:cfg:create");

			Flock.beginTime("FlockIncremental@replaceNode:cfg:gatherNeighbours");
			Set<Node> predecessors = new HashSet<>(removedNodes.size());
			Set<Node> successors = new HashSet<>(removedNodes.size());
			for (Node n : removedNodes) {
				predecessors.addAll(this.graph.parentsOf(n));
				successors.addAll(this.graph.childrenOf(n));
			}
			predecessors.removeAll(removedNodes);
			successors.removeAll(removedNodes);
			Flock.endTime("FlockIncremental@replaceNode:cfg:gatherNeighbours");

			Flock.beginTime("FlockIncremental@replaceNode:cfg:graphReplace");
			this.graph.replaceNodes(removedNodes, predecessors, successors, subGraph);
			Flock.endTime("FlockIncremental@replaceNode:cfg:graphReplace");

			Flock.beginTime("FlockIncremental@replaceNode:cfg:graphScssReplace");
			if (changedIrregular) {
				this.graph_scss.recompute(this.graph);
			} else {
				this.graph_scss.replaceNodes(this.graph, removedNodes, predecessors, successors, subGraph);
			}
			Flock.endTime("FlockIncremental@replaceNode:cfg:graphScssReplace");

			this.graph_scss.validate(this.graph);

			// This is some useful validation logic when looking for bugs in SCC creation

//			if (Flock.DEBUG) {
//				SCCs new_scss = new SCCs(this.graph);
//				if (new_scss.components.size() != this.graph_scss.components.size()) {
//					throw new RuntimeException("Inc. vs from-scratch SCCs don't match");
//				}
//			}

			for (Node n : subGraph.nodes()) {
				this.addToNew(n);
			}

			if (changedIrregular) {
				Flock.beginTime("FlockIncremental@removeAnalysisResultsIrregular");
				{
					/*
					 * We do the removal of analysis results again because the update was irregular.
					 * This means nodes may now depend on the new subgraph whereas they did not
					 * depend on the replaced subgraph previously.
					 */
					for (Analysis a : this.analyses) {
						this.removeAnalysisResultsAfter(a, subGraph.nodes());
					}
				}
				Flock.endTime("FlockIncremental@removeAnalysisResultsIrregular");
			}
		}
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		Flock.endTime("FlockIncremental@replaceNode:cfg");
		this.validate();
		Flock.endTime("FlockIncremental@replaceNode");
	}

	@Override
	public void removeNode(IStrategoTerm node) {
		Flock.increment("removeNode");
		Flock.beginTime("FlockIncremental@removeNode");
		Flock.log("api", "removing node " + node.toString());

		Set<Node> removedNodes = getAllNodes(node);
		boolean changedIrregular = this.termTree.containsIrregularTerm(Helpers.getTermId(node));
		this.termTree.remove(Helpers.getTermId(node));

		if (changedIrregular) {
			Set<Component> changed = this.graph_scss.recompute(this.graph);
			this.removeResultsAfter(changed);
		} else {
			for (Analysis a : this.analyses) {
				this.removeAnalysisResultsAfter(a, removedNodes);
			}

			// Go through graph and remove facts with origin in removed id's
			this.applyGhostMask(removedNodes);
			this.graph.removeGhostNodes();
		}

		this.graph_scss.validate(this.graph);

		Flock.endTime("FlockIncremental@removeNode");
	}

	private void removeResultsAfter(Set<Component> cs) {
		for (Analysis a : this.analyses) {
			for (Component c : cs) {
				this.removeAnalysisResultsAfter(a, c);
			}
		}
	}

	private void removeAnalysisResultsAfter(Analysis a, Component c) {
		Set<Component> reachable = new HashSet<>();
		reachable.add(c);
		if (a.direction == Direction.FORWARD) {
			this.collectSuccessors(a, c, reachable);
		} else {
			this.collectPredecessors(a, c, reachable);
		}

		for (Component c2 : reachable) {
			this.removeAnalysisResultsIn(a, c2);
		}
	}

	private void removeAnalysisResultsAfter(Analysis a, Collection<Node> removedNodes) {
		Set<Component> outdatedComponents = removedNodes.stream().map(this.graph_scss.nodeComponent::get)
				.collect(Collectors.toSet());

		for (Component c : outdatedComponents)
			this.removeAnalysisResultsAfter(a, c);

		for (Node n : removedNodes) {
			a.remove(n);
		}
	}

	private void removeAnalysisResultsIn(Analysis a, Component c) {
		a.removeNodeResults(this.graph_scss, c.nodes);
	}

	private void collectPredecessors(Analysis a, Component c, Set<Component> result) {
		for (Component neighbour : this.graph_scss.revNeighbours.get(c)) {
			if (!a.dirtyComponents.contains(c) && !result.contains(neighbour)) {
				result.add(neighbour);
				collectPredecessors(a, neighbour, result);
			}
		}
	}

	private void collectSuccessors(Analysis a, Component c, Set<Component> result) {
		for (Component neighbour : this.graph_scss.neighbours.get(c)) {
			if (!a.dirtyComponents.contains(c) && !result.contains(neighbour)) {
				result.add(neighbour);
				collectSuccessors(a, neighbour, result);
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
