package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import org.metaborg.lang.tiger.flock.common.Graph.Node;

public class SCCs {
	public class Component {
		Set<Node> nodes = new HashSet<>();
	}

	List<Component> components = new ArrayList<>();
	HashMap<Component, Set<Component>> neighbours = new HashMap<>();
	HashMap<Component, Set<Component>> revNeighbours = new HashMap<>();
	HashMap<Node, Component> nodeComponent = new HashMap<>();
	boolean DEBUG = true;

	public SCCs(Graph g) {
		Flock.beginTime("SCCs@computeIntervals");

		// Using AtomicLong to pass mutable reference
		AtomicLong next_index = new AtomicLong(1);
		Stack<Node> S = new Stack<>();
		HashSet<Node> onStack = new HashSet<>();
		HashMap<Node, Long> lowlink = new HashMap<>();
		HashMap<Node, Long> index = new HashMap<>();

		for (Node n : g.roots()) {
			if (index.get(n) == null) {
				strongConnect(g, n, next_index, S, onStack, lowlink, index, components, nodeComponent);
			}
		}

		for (Component c : this.components) {
			this.neighbours.put(c, new HashSet<>());
			this.revNeighbours.put(c, new HashSet<>());
		}

		for (Node n : g.nodes()) {
			Component nComp = this.nodeComponent.get(n);
			assert nComp != null;

			for (Node v : g.childrenOf(n)) {
				Component vComp = this.nodeComponent.get(v);
				assert vComp != null;

				if (vComp != nComp) {
					this.makeEdge(nComp, vComp);
				}
			}
		}

		Flock.endTime("SCCs@computeIntervals");
	}

	/*
	 * 
	 * This gives a nullpointer exception Because The subgraph is already merged
	 * into the full graph Which mutates some of the data in the subgraph Such that
	 * the subgraph by itself is not valid anymore
	 * 
	 * Either fix this so that graph merging leaves the old fully valid (and
	 * independent) Or work around it
	 */

	public void replaceNodes(Graph graph, Set<Node> replaced, Set<Node> oldPredecessors, Set<Node> oldSuccessors,
			Graph subgraph) {

		Set<Component> commonSCCs = new HashSet<>();
		for (Node pred : oldPredecessors) {
			commonSCCs.add(this.nodeComponent.get(pred));
		}
		Set<Component> successorSCCs = new HashSet<>();
		for (Node suc : oldSuccessors) {
			successorSCCs.add(this.nodeComponent.get(suc));
		}
		commonSCCs.retainAll(successorSCCs);

		// Case 1: The replaced subgraph is part of a greater SCC
		// So the new subgraph is also part of the same SCC
		if (commonSCCs.size() == 1) {
			Component commonComponent = commonSCCs.iterator().next();
			for (Node n : subgraph.nodes()) {
				commonComponent.nodes.add(n);
				this.nodeComponent.put(n, commonComponent);
			}
		}
		// Case 1.5: Multiple SCCs found, this should not happen
		else if (commonSCCs.size() > 1) {
			throw new RuntimeException("Replaced graph was part of multiple SCCs");
		}
		// Case 2: The replaced subgraph is not part of a greater SCC
		// So we compute the SCCs that make up the subgraph and merge them into the
		// larger SCCs
		else {

			SCCs subgraphSCCs = new SCCs(subgraph);

			this.mergeSCCs(subgraphSCCs);

			// Add edges between old predecessors and new root components
			for (Node root : subgraph.roots()) {
				Component rootScc = this.nodeComponent.get(root);

				for (Node pred : oldPredecessors) {
					Component predScc = this.nodeComponent.get(pred);

					this.makeEdge(predScc, rootScc);
				}
			}

			// Add edges between new leaf and old successor components
			for (Node root : subgraph.leaves()) {
				Component leafScc = this.nodeComponent.get(root);

				for (Node suc : oldSuccessors) {
					Component sucScc = this.nodeComponent.get(suc);

					this.makeEdge(leafScc, sucScc);
				}
			}

		}

		for (Node n : replaced) {
			this.removeComponent(this.nodeComponent.get(n));
		}
	}
	
	public Set<Component> predecessors(Component a) {
		HashSet<Component> res = new HashSet<>();
		this.predecessors(a, res);
		return res;
	}
	
	private void predecessors(Component a, Set<Component> result) {
		result.addAll(this.revNeighbours.get(a));
		for (Component p : this.revNeighbours.get(a)) {
			this.predecessors(p, result);
		}
	}

	private void makeEdge(Component a, Component b) {
		this.neighbours.get(a).add(b);
		this.revNeighbours.get(b).add(a);
	}

	private void mergeSCCs(SCCs other) {
		this.components.addAll(other.components);
		this.neighbours.putAll(other.neighbours);
		this.revNeighbours.putAll(other.revNeighbours);
		this.nodeComponent.putAll(other.nodeComponent);
	}

	private void removeComponent(Component c) {
		for (Node n : c.nodes) {
			this.nodeComponent.remove(n);
		}
		for (Component revNeighbour : this.revNeighbours.get(c)) {
			this.neighbours.get(revNeighbour).remove(c);
		}
		for (Component neighbour : this.neighbours.get(c)) {
			this.revNeighbours.get(neighbour).remove(c);
		}
		
		this.components.remove(c);
		this.neighbours.remove(c);
		this.revNeighbours.remove(c);
	}

	// Tarjans
	private void strongConnect(Graph g, Node v, AtomicLong next_index, Stack<Node> S, HashSet<Node> onStack,
			HashMap<Node, Long> lowlink, HashMap<Node, Long> index, List<Component> components,
			HashMap<Node, Component> nodeComponent) {
		index.put(v, next_index.longValue());
		lowlink.put(v, next_index.longValue());
		next_index.addAndGet(1);
		S.push(v);
		onStack.add(v);
		for (Node w : g.childrenOf(v)) {
			if (index.get(w) == null) {
				strongConnect(g, w, next_index, S, onStack, lowlink, index, components, nodeComponent);
				if (lowlink.get(w) < lowlink.get(v)) {
					lowlink.put(v, lowlink.get(w));
				}
			} else {
				if (onStack.contains(w)) {
					if (index.get(w) < lowlink.get(v)) {
						lowlink.put(v, index.get(w));
					}
				}
			}
		}

		if (lowlink.get(v).equals(index.get(v))) {
			Node w;
			Component component = this.new Component();
			do {
				w = S.pop();
				onStack.remove(w);
				nodeComponent.put(w, component);
				component.nodes.add(w);
			} while (w != v);
			components.add(component);
		}
	}

	public void validate(Graph g) {
		if (!DEBUG)
			return;

		for (Node n : g.nodes()) {
			if (this.nodeComponent.get(n) == null) {
				throw new RuntimeException("Node component entry missing");
			}
		}

		if (g.nodes().size() != this.nodeComponent.size()) {
			throw new RuntimeException("Node sizes not matching");
		}

		for (Component c : this.components) {
			for (Component d : this.neighbours.get(c)) {
				if (!this.revNeighbours.get(d).contains(c)) {
					throw new RuntimeException("Missing reverse neighbour");
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("[");
		int ci = 0;
		for (Component c : this.components) {
			s.append("{");
			int i = 0;
			for (Node n : c.nodes) {
				s.append(n.getId());
				if (i < c.nodes.size() - 1)
					s.append(",");
				i++;
			}
			s.append("}");
			if (ci < this.components.size() - 1)
				s.append(",");
			ci++;
		}
		s.append("]");
		return s.toString();
	}

	public String toGraphviz() {
		StringBuilder result = new StringBuilder();
		result.append(
				"digraph G { graph[rankdir=LR, center=true, margin=0.2, nodesep=0.1, ranksep=0.3]; node[shape=record];");
		int i = 0;
		HashMap<Component, Integer> componentIds = new HashMap<>();

		for (Component c : this.components) {
			componentIds.put(c, i);

			result.append(i);
			result.append("; ");
			i++;
		}
		for (Component c : this.components) {
			for (Component n : this.neighbours.get(c)) {
				result.append(componentIds.get(c));
				result.append("->");
				result.append(componentIds.get(n));
				result.append("; ");
			}
		}
		result.append("} ");
		return result.toString();
	}
}
