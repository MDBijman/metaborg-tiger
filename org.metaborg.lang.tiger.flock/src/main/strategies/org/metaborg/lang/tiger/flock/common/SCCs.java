package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.spoofax.terms.util.NotImplementedException;

public class SCCs {
	public class Component {
		Set<Node> nodes = new HashSet<>();
		Set<Component> parents = new HashSet<>();
		Set<Component> children = new HashSet<>();
		boolean clean = false;
	}

	HashSet<Component> components = new HashSet<>();
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
			Component matchingComponent = c.nodes.iterator().next().component;
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
				n.component = new_here;
			}
		}

		computeComponentEdges(graph);

		Flock.endTime("SCCS@recompute");

		return new HashSet<>(changedMapping.values());
	}

	public void removeNodes(Graph graph, Set<Node> replaced, Set<Node> oldPredecessors, Set<Node> oldSuccessors) {
		throw new NotImplementedException();
	}

	public Component commonSCCs(Set<Node> oldPredecessors, Set<Node> oldSuccessors) {
		Flock.beginTime("SCCs@replaceNodes:findCommonSCCs");
		Set<Component> predecessorSCCs = new HashSet<>();
		Set<Component> successorSCCs = new HashSet<>();

		// Get common components between predecessors and successors
		for (Node pred : oldPredecessors) {
			predecessorSCCs.add(pred.component);
		}

		for (Node suc : oldSuccessors) {
			successorSCCs.add(suc.component);
		}
		Set<Component> commonSCCs = predecessorSCCs;
		commonSCCs.retainAll(successorSCCs);
		Flock.endTime("SCCs@replaceNodes:findCommonSCCs");
		// Multiple SCCs found, this should not happen
		if (commonSCCs.size() > 1) {
			throw new RuntimeException("Replaced graph was part of multiple SCCs");
		}

		if (commonSCCs.size() == 0) {
			return null;
		}

		return commonSCCs.iterator().next();
	}

	/*
	 * commonComponent may be null, if there is not common component if
	 * commonComponent is null, then subgraphSCCs must not be null
	 */
	public void replaceNodes(Graph graph, Set<Node> replaced, Set<Node> oldPredecessors, Set<Node> oldSuccessors,
			Graph subgraph, Component commonComponent, SCCs subgraphSCCs) {
		Flock.beginTime("SCCs@replaceNodes");
		// Case 1: The replaced subgraph is part of a greater SCC
		// So the new subgraph is also part of the same SCC
		if (commonComponent != null) {
			for (Node n : subgraph.nodes()) {
				commonComponent.nodes.add(n);
				n.component = commonComponent;
			}

			// Remove information of replaced nodes
			for (Node n : replaced) {
				n.component.nodes.remove(n);
				n.component = null;
			}
		}
		// Case 2: The replaced subgraph is not part of a greater SCC
		// So we compute the SCCs that make up the subgraph and merge them into the
		// larger SCCs
		else {
			Flock.beginTime("SCCs@replaceNodes:mergeSCCs");
			this.mergeSCCs(subgraphSCCs);
			Flock.endTime("SCCs@replaceNodes:mergeSCCs");

			Flock.beginTime("SCCs@replaceNodes:linkSCCpred");
			// Add edges between old predecessors and new root components
			Component rootScc = subgraph.getEntry().component;

			for (Node pred : oldPredecessors) {
				Component predScc = pred.component;

				this.makeEdge(predScc, rootScc);
			}
			Flock.endTime("SCCs@replaceNodes:linkSCCpred");

			Flock.beginTime("SCCs@replaceNodes:linkSCCsucc");
			// Add edges between new leaf and old successor components
			Component leafScc = subgraph.getExit().component;

			for (Node suc : oldSuccessors) {
				Component sucScc = suc.component;

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
				this.removeComponent(n.component);
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
		result.addAll(a.parents);
		for (Component p : a.parents) {
			this.predecessors(p, result);
		}
	}

	private Component makeComponent() {
		Component c = new Component();
		this.components.add(c);
		return c;
	}

	private void makeEdge(Component a, Component b) {
		a.children.add(b);
		b.parents.add(a);
	}

	private void mergeSCCs(SCCs other) {
		this.components.addAll(other.components);
	}

	private void removeComponent(Component c) {
		for (Node n : c.nodes) {
			n.component = null;
		}
		for (Component parent : c.parents) {
			parent.children.remove(c);
		}
		for (Component child : c.children) {
			child.parents.remove(c);
		}

		c.children.clear();
		c.parents.clear();
		this.components.remove(c);
	}

	private void computeSCCsFromEntry(Graph g) {
		// Using AtomicLong to pass mutable reference
		Stack<Node> S = new Stack<>();
		HashSet<Node> onStack = new HashSet<>();
		HashMap<Node, Long> lowlink = new HashMap<>();
		HashMap<Node, Long> index = new HashMap<>();

		long nextIndex = 1;
		if (index.get(g.getEntry()) == null) {
			nextIndex = strongConnect(g, g.getEntry(), nextIndex, S, onStack, lowlink, index, components);
		}

		// There are dangling nodes (i.e. dead code)
		// But we have to deal with these as well
		if (index.size() != g.nodes().size()) {
			for (Node n : g.nodes()) {
				if (index.get(n) == null && !n.equals(g.getStart()) && !n.equals(g.getEnd())) {
					nextIndex = strongConnect(g, n, nextIndex, S, onStack, lowlink, index, components);
				}
			}
		}

		computeComponentEdges(g);
	}

	private void computeSCCsFromStart(Graph g) {
		// Using AtomicLong to pass mutable reference
		Stack<Node> S = new Stack<>();
		HashSet<Node> onStack = new HashSet<>();
		HashMap<Node, Long> lowlink = new HashMap<>();
		HashMap<Node, Long> index = new HashMap<>();

		long nextIndex = 1;
		if (index.get(g.getStart()) == null) {
			nextIndex = strongConnect(g, g.getStart(), nextIndex, S, onStack, lowlink, index, components);
		}

		// There are dangling nodes (i.e. dead code)
		// But we have to deal with these as well
		if (index.size() != g.nodes().size()) {
			for (Node n : g.nodes()) {
				if (index.get(n) == null) {
					nextIndex = strongConnect(g, n, nextIndex, S, onStack, lowlink, index, components);
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
			computeComponentEdges(g, c);
		}
	}

	/*
	 * Computes edges to neighboring components.
	 */
	private void computeComponentEdges(Graph g, Component c) {
		for (Node n : c.nodes) {
			Component nComp = n.component;

			for (Node v : n.children) {
				Component vComp = v.component;

				if (vComp != nComp) {
					this.makeEdge(nComp, vComp);
				}
			}

		}
	}

	// Tarjans
	private long strongConnect(Graph g, Node v, long next_index, Stack<Node> S, HashSet<Node> onStack,
			HashMap<Node, Long> lowlink, HashMap<Node, Long> index, HashSet<Component> components) {
		Flock.beginTime("SCCs@strongConnect");
		Stack<Pair<Node, Node>> stack = new Stack<>();

		S.push(v);
		stack.push(Pair.of(null, v));
		onStack.add(v);

		while (!stack.isEmpty()) {
			Pair<Node, Node> p = stack.peek();
			Node n = p.getRight();

			next_index += 1;
			index.put(n, next_index);
			lowlink.put(n, next_index);

			boolean pushedChildren = false;

			for (Node w : n.children) {
				if (index.get(w) == null) {
					stack.push(Pair.of(n, w));
					S.push(w);
					onStack.add(w);
					pushedChildren = true;
				} else if (onStack.contains(w)) {
					if (index.get(w) < lowlink.get(v)) {
						lowlink.put(v, index.get(w));
					}
				}
			}

			if (pushedChildren) {
				continue;
			}

			p = stack.pop();
			Node pred = p.getLeft();

			if (pred != null) {
				if (lowlink.get(n) < lowlink.get(pred)) {
					lowlink.put(pred, lowlink.get(n));
				}
			}

			if (lowlink.get(n).equals(index.get(n))) {
				Node w;
				Component component = this.new Component();
				do {
					w = S.pop();
					onStack.remove(w);
					w.component = component;
					component.nodes.add(w);
				} while (w != n);
				components.add(component);
			}
		}
		Flock.endTime("SCCs@strongConnect");
		return next_index;
	}

	public void validate(Graph g) {
		if (!DEBUG)
			return;

		for (Node n : g.nodes()) {
			if (n.component == null) {
				throw new RuntimeException("Node component entry missing for " + n.toString());
			}
		}

		for (Component c : this.components) {
			for (Component d : c.children) {
				if (!d.parents.contains(c)) {
					throw new RuntimeException("Missing reverse neighbour");
				}
			}
			if (c.nodes.size() == 0)
				throw new RuntimeException("Component without nodes");
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
				result.append("[label=\"<f0>" + c.nodes.iterator().next().toString() + "|<f1>" + c.clean + "\"]");
			}
			result.append("; ");
			i++;
		}
		for (Component c : this.components) {
			for (Component n : c.children) {
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
