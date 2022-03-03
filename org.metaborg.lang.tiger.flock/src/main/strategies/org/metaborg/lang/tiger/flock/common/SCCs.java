package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.spoofax.terms.util.NotImplementedException;

public class SCCs {
	public class Component {
		Set<Node> nodes = new HashSet<>();
	}

	HashSet<Component> components = new HashSet<>();
	HashMap<Component, Set<Component>> neighbours = new HashMap<>();
	HashMap<Component, Set<Component>> revNeighbours = new HashMap<>();
	HashMap<Node, Component> nodeComponent = new HashMap<>();
	private static final boolean DEBUG = Flock.DEBUG;

	public SCCs() {

	}

	public static SCCs startingFromEntry(Graph g) {
		Flock.beginTime("SCCs@constructor");
		SCCs r = new SCCs();
		r.computeSCCsFromEntry(g);
		Flock.endTime("SCCs@constructor");
		return r;
	}

	public static SCCs startingFromStart(Graph g) {
		Flock.beginTime("SCCs@constructor");
		SCCs r = new SCCs();
		r.computeSCCsFromStart(g);
		Flock.endTime("SCCs@constructor");
		return r;
	}

	/*
	 * Returns the changed components.
	 */
	public Set<Component> recompute(Graph graph) {
		Flock.beginTime("SCCS@recompute");
		SCCs new_scss = SCCs.startingFromStart(graph);
		Set<Component> unchangedComponents = new HashSet<>();
		Set<Component> newComponents = new HashSet<>();
		/* maps new to old */
		HashMap<Component, Component> unchangedMapping = new HashMap<>();
		/*
		 * Here we try to determine which sccs are unchanged.
		 */
		comp_loop: for (Component c : new_scss.components) {
			// Get one of the matching components of this sccs of one of the nodes of c
			// That component should match c
			Component matchingComponent = this.nodeComponent.get(c.nodes.iterator().next());
			if (matchingComponent == null || matchingComponent.nodes.size() != c.nodes.size()) {
				newComponents.add(c);
				continue comp_loop;
			}
			for (Node n : matchingComponent.nodes) {
				if (!c.nodes.contains(n)) {
					newComponents.add(c);
					continue comp_loop;
				}
			}
			unchangedMapping.put(c, matchingComponent);
			unchangedComponents.add(matchingComponent);
		}

		/*
		 * We remove changed components from this sccs
		 */
		Set<Component> changedComponents = new HashSet<>();

		for (Component c : this.components) {
			if (unchangedComponents.contains(c))
				continue;
			changedComponents.add(c);
		}

		for (Component c : changedComponents) {
			this.removeComponent(c);
		}

		/*
		 * We create new components for all changed sccs, that is, the components of
		 * new_sccs which do not map to one of our own.
		 */

		/*
		 * maps new components from new_scss to corresponding new components in this
		 * scss
		 */
		HashMap<Component, Component> changedMapping = new HashMap<>();
		for (Component c : newComponents) {
			Component new_here = this.makeComponent();
			changedMapping.put(c, new_here);
			for (Node n : c.nodes) {
				new_here.nodes.add(n);
				this.nodeComponent.put(n, new_here);
			}
		}

		computeComponentEdges(graph);

		Flock.endTime("SCCS@recompute");

		return new HashSet<>(changedMapping.values());
	}

	public void removeNodes(Graph graph, Set<Node> replaced, Set<Node> oldPredecessors, Set<Node> oldSuccessors) {
		throw new NotImplementedException();
	}

	public void replaceNodes(Graph graph, Set<Node> replaced, Set<Node> oldPredecessors, Set<Node> oldSuccessors,
			Graph subgraph) {
		Flock.beginTime("SCCs@replaceNodes");

		Flock.beginTime("SCCs@replaceNodes:findCommonSCCs");
		Set<Component> predecessorSCCs = new HashSet<>();
		Set<Component> successorSCCs = new HashSet<>();

		// Get common components between predecessors and successors
		for (Node pred : oldPredecessors) {
			predecessorSCCs.add(this.nodeComponent.get(pred));
		}

		for (Node suc : oldSuccessors) {
			successorSCCs.add(this.nodeComponent.get(suc));
		}
		Set<Component> commonSCCs = predecessorSCCs;
		commonSCCs.retainAll(successorSCCs);
		Flock.endTime("SCCs@replaceNodes:findCommonSCCs");

		// Case 1: The replaced subgraph is part of a greater SCC
		// So the new subgraph is also part of the same SCC
		if (commonSCCs.size() == 1) {
			Component commonComponent = commonSCCs.iterator().next();
			for (Node n : subgraph.nodes()) {
				commonComponent.nodes.add(n);
				this.nodeComponent.put(n, commonComponent);
			}

			// Remove information of replaced nodes
			for (Node n : replaced) {
				this.nodeComponent.get(n).nodes.remove(n);
				this.nodeComponent.remove(n);
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
			Flock.beginTime("SCCs@replaceNodes:makeSubSCCs");
			SCCs subgraphSCCs = SCCs.startingFromEntry(subgraph);
			Flock.endTime("SCCs@replaceNodes:makeSubSCCs");

			Flock.beginTime("SCCs@replaceNodes:mergeSCCs");
			this.mergeSCCs(subgraphSCCs);
			Flock.endTime("SCCs@replaceNodes:mergeSCCs");

			Flock.beginTime("SCCs@replaceNodes:linkSCCpred");
			// Add edges between old predecessors and new root components
			Component rootScc = this.nodeComponent.get(subgraph.getEntry());

			for (Node pred : oldPredecessors) {
				Component predScc = this.nodeComponent.get(pred);

				this.makeEdge(predScc, rootScc);
			}
			Flock.endTime("SCCs@replaceNodes:linkSCCpred");

			Flock.beginTime("SCCs@replaceNodes:linkSCCsucc");
			// Add edges between new leaf and old successor components
			Component leafScc = this.nodeComponent.get(subgraph.getExit());

			for (Node suc : oldSuccessors) {
				Component sucScc = this.nodeComponent.get(suc);

				this.makeEdge(leafScc, sucScc);
			}
			Flock.endTime("SCCs@replaceNodes:linkSCCsucc");

			Flock.beginTime("SCCs@replaceNodes:removeOld");
			// Remove the SCCs that were replaced by the program edit
			// This relies on the assumption that all components containing any of these
			// nodes are fully removed
			// Intuitively this should be true because we cannot replace part of a SCC when
			// these are only created
			// from a single program node (e.g. for-loop). But this does not generalize to
			// more complex CFGs.
			for (Node n : replaced) {
				this.removeComponent(this.nodeComponent.get(n));
			}
			Flock.endTime("SCCs@replaceNodes:removeOld");
		}

		Flock.endTime("SCCs@replaceNodes");
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

	private Component makeComponent() {
		Component c = new Component();
		this.components.add(c);
		this.neighbours.put(c, new HashSet<>());
		this.revNeighbours.put(c, new HashSet<>());
		return c;
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

	private void computeSCCsFromEntry(Graph g) {
		// Using AtomicLong to pass mutable reference
		AtomicLong next_index = new AtomicLong(1);
		Stack<Node> S = new Stack<>();
		HashSet<Node> onStack = new HashSet<>();
		HashMap<Node, Long> lowlink = new HashMap<>();
		HashMap<Node, Long> index = new HashMap<>();

		if (index.get(g.getEntry()) == null) {
			strongConnect(g, g.getEntry(), next_index, S, onStack, lowlink, index, components, nodeComponent);
		}

		// There are dangling nodes (i.e. dead code)
		// But we have to deal with these as well
		if (index.size() != g.nodes().size()) {
			for (Node n : g.nodes()) {
				if (index.get(n) == null && !n.equals(g.getStart()) && !n.equals(g.getEnd())) {
					strongConnect(g, n, next_index, S, onStack, lowlink, index, components, nodeComponent);
				}
			}
		}

		computeComponentEdges(g);
	}

	private void computeSCCsFromStart(Graph g) {
		// Using AtomicLong to pass mutable reference
		AtomicLong next_index = new AtomicLong(1);
		Stack<Node> S = new Stack<>();
		HashSet<Node> onStack = new HashSet<>();
		HashMap<Node, Long> lowlink = new HashMap<>();
		HashMap<Node, Long> index = new HashMap<>();

		if (index.get(g.getStart()) == null) {
			strongConnect(g, g.getStart(), next_index, S, onStack, lowlink, index, components, nodeComponent);
		}

		// There are dangling nodes (i.e. dead code)
		// But we have to deal with these as well
		if (index.size() != g.nodes().size()) {
			for (Node n : g.nodes()) {
				if (index.get(n) == null) {
					strongConnect(g, n, next_index, S, onStack, lowlink, index, components, nodeComponent);
				}
			}
		}

		computeComponentEdges(g);
	}

	/*
	 * Computes edges between components based on edges between nodes.
	 */
	private void computeComponentEdges(Graph g) {
		for (Component c : this.components) {
			this.neighbours.put(c, new HashSet<>());
			this.revNeighbours.put(c, new HashSet<>());
		}

		for (Component c : this.components) {
			computeComponentEdges(g, c);
		}
	}

	/*
	 * Computes edges to neighboring components.
	 */
	private void computeComponentEdges(Graph g, Component c) {
		for (Node n : c.nodes) {
			Component nComp = this.nodeComponent.get(n);

			for (Node v : g.childrenOf(n)) {
				Component vComp = this.nodeComponent.get(v);

				if (vComp != nComp) {
					this.makeEdge(nComp, vComp);
				}
			}

		}
	}

	// Tarjans
	private void strongConnect(Graph g, Node v, AtomicLong next_index, Stack<Node> S, HashSet<Node> onStack,
			HashMap<Node, Long> lowlink, HashMap<Node, Long> index, HashSet<Component> components,
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
				throw new RuntimeException("Node component entry missing for " + n.toString());
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
			if (c.nodes.size() == 1) {
				result.append("[label=\"" + c.nodes.iterator().next().toString() + "\"]");
			}
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
