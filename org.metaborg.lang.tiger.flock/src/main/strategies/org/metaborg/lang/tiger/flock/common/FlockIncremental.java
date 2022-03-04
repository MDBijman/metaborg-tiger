package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;
import org.metaborg.lang.tiger.flock.common.IAnalysis.Direction;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.metaborg.lang.tiger.flock.graph.GraphFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class FlockIncremental extends Flock {

	public FlockIncremental() {

	}

	private void validate() {
		this.termTree.validate();
		this.graph.validate();
		for (IAnalysis a : this.analyses) {
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
		this.graph.validate();
		initPosition(graph, context.getFactory());
		this.graph_scss = SCCs.startingFromStart(this.graph);
	}

	@Override
	public void replaceNode(IStrategoTerm current, IStrategoTerm replacement) {
		Flock.increment("replaceNode");
		Flock.beginTime("FlockIncremental@replaceNode");

		this.validate();

		Flock.beginTime("FlockIncremental@replaceNode:updateTermTree");
		// Gather ids and compute regularity
		TermId rootId = Helpers.getTermId(current);
		Set<TermId> innerIds = getAllIds(current);
		boolean irregular = false;
		for (TermId t : innerIds) {
			irregular |= this.termTree.isIrregular(t);
		}

		// Replace nodes in tree
		this.termTree.replace(Helpers.getTermId(current), replacement);
		Flock.endTime("FlockIncremental@replaceNode:updateTermTree");

		// Also check if any of the new nodes are irregular
		for (TermId t : getAllIds(replacement)) {
			irregular |= this.termTree.isIrregular(t);
		}

		// Root id is separate
		innerIds.remove(rootId);

		this.termTree.validate();

		// Remove analysis results reachable from node
		Set<Node> removedNodes = getAllNodes(current);
		Flock.beginTime("FlockIncremental@replaceNode:removeAnalysisResults");
		this.removeAnalysisResults(removedNodes);
		Flock.endTime("FlockIncremental@replaceNode:removeAnalysisResults");
		Flock.beginTime("FlockIncremental@replaceNode:removeNodesInAnalyses");
		this.removeNodesInAnalyses(removedNodes);
		Flock.beginTime("FlockIncremental@replaceNode:removeNodesInAnalyses");

		Flock.beginTime("FlockIncremental@replaceNode:createCfg");
		// Create new sub-CFG
		Graph subGraph = GraphFactory.createCfgOnce(this.termTree, replacement);
		Flock.endTime("FlockIncremental@replaceNode:createCfg");

		// Inefficient fallback for SCC replacement
		if (irregular) {
			// Replace nodes in CFG
			this.graph.replaceNodes(rootId, innerIds, subGraph);

			// Replace nodes in SCC
			this.graph_scss.recompute(this.graph);

			// Remove analysis if irregular because other existing components may become
			// reachable
			// TODO remove analysis results again
		} else {
			// Efficient
			Flock.beginTime("FlockIncremental@replaceNode:gatherNeighbours");
			Set<Node> predecessors = new HashSet<>(removedNodes.size());
			Set<Node> successors = new HashSet<>(removedNodes.size());
			for (Node n : removedNodes) {
				predecessors.addAll(n.parents);
				successors.addAll(n.children);
			}
			predecessors.removeAll(removedNodes);
			successors.removeAll(removedNodes);
			Flock.endTime("FlockIncremental@replaceNode:gatherNeighbours");

			// Before we change the CFG we must record some SCC information
			Component common = this.graph_scss.commonSCCs(predecessors, successors);
			Flock.beginTime("SCCs@replaceNodes:makeSubSCCs");
			// We only compute SCCs if there was no common component
			SCCs subgraphSCCs = common == null ? SCCs.startingFromEntry(subGraph) : null;
			Flock.endTime("SCCs@replaceNodes:makeSubSCCs");

			Flock.beginTime("FlockIncremental@replaceNode:replaceCFG");
			// Replace nodes in CFG
			this.graph.replaceNodes(rootId, innerIds, subGraph);
			Flock.endTime("FlockIncremental@replaceNode:replaceCFG");

			Flock.beginTime("FlockIncremental@replaceNode:replaceSCC");

			// Efficient update of SCCs
			this.graph_scss.replaceNodes(this.graph, removedNodes, predecessors, successors, subGraph, common,
					subgraphSCCs);
			Flock.endTime("FlockIncremental@replaceNode:replaceSCC");
		}

		Flock.beginTime("FlockIncremental@replaceNode:addToNew");
		// Add new nodes to analysis
		for (Node n : subGraph.nodes()) {
			if (n.type != NodeType.START && n.type != NodeType.END)
				this.addToNew(n);
		}
		Flock.endTime("FlockIncremental@replaceNode:addToNew");

		this.graph_scss.validate(this.graph);

		this.validate();

		Flock.endTime("FlockIncremental@replaceNode");
	}

	@Override
	public void removeNode(IStrategoTerm node) {
		Flock.increment("removeNode");
		Flock.beginTime("FlockIncremental@removeNode");
		Flock.log("api", "removing node " + node.toString());

		// Remove nodes in tree
		this.termTree.remove(Helpers.getTermId(node));

		this.termTree.validate();

		// Remove analysis results reachable from node
		Set<Node> removedNodes = getAllNodes(node);
		this.removeAnalysisResults(removedNodes);
		this.removeNodesInAnalyses(removedNodes);

		TermId rootId = Helpers.getTermId(node);
		Set<TermId> innerIds = getAllIds(node);
		innerIds.remove(rootId);

		// Remove nodes in CFG
		this.graph.removeNodes(rootId, innerIds);

		// Remove nodes in SCC
		Set<Component> changed = this.graph_scss.recompute(this.graph);

		// Remove analysis result reachable from node (again)
		// because irregular updates may give new reachable nodes
		this.removeAnalysisResults(changed);

		this.graph_scss.validate(this.graph);

		Flock.endTime("FlockIncremental@removeNode");
	}

	private void removeNodesInAnalyses(Collection<Node> removedNodes) {
		for (IAnalysis a : this.analyses) {
			for (Node n : removedNodes) {
				a.remove(n);
			}
		}
	}

	private void removeAnalysisResults(Set<Component> cs) {
		for (Component c : cs) {
			this.removeAnalysisResults(c);
		}
	}

	private void removeAnalysisResults(Collection<Node> removedNodes) {
		Set<Component> outdatedComponents = removedNodes.stream().map(this.graph_scss.nodeComponent::get)
				.collect(Collectors.toSet());

		for (Component c : outdatedComponents) {
			this.removeAnalysisResults(c);
		}
	}

	private void removeAnalysisResults(Component c) {
		for (IAnalysis a : this.analyses) {
			a.removeAnalysisResults(this.graph_scss, c);
		}
	}

	@Override
	public Node getNode(TermId id) {
		return graph.getNode(id);
	}

	@Override
	public IAnalysis analysisWithName(String name) {
		for (IAnalysis a : analyses) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		throw new RuntimeException("No analysis with name " + name);
	}

}
