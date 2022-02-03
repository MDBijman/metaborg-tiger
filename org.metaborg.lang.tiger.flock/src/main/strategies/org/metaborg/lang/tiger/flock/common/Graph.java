package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;

public class Graph {
	public static class Node {
		private TermId id;

		public boolean isGhost = false;
		public ITerm virtualTerm = null;
		public HashMap<String, Property> properties = new HashMap<>();
		public Graph graph = null;

		public Node(Graph parent) {
			this.isGhost = true;
			this.id = Flock.nextNodeId();
			this.graph = parent;
		}

		public Node(Graph parent, Node other) {
			this.isGhost = other.isGhost;
			this.id = other.id;
			this.properties = other.properties;
			this.virtualTerm = other.virtualTerm;
			this.graph = parent;
		}

		public Node(Graph parent, TermId id) {
			this.id = id;
			this.graph = parent;
		}

		public Node(Graph parent, TermId id, ITerm t) {
			this.id = id;
			this.virtualTerm = t;
			this.graph = parent;
		}

		public Node withGraph(Graph parent) {
			this.graph = parent;
			return this;
		}

		public void addProperty(String name, FlockLattice lat) {
			properties.put(name, new Property(name, lat));
		}

		public Property getProperty(String name) {
			return properties.get(name);
		}

		public Collection<Property> properties() {
			return properties.values();
		}

		public Collection<Node> predecessors(Analysis.Direction dir) {
			if (dir == Analysis.Direction.FORWARD) {
				return graph.parentsOf(this);
			} else {
				return graph.childrenOf(this);
			}
		}

		@Override
		public String toString() {
			return "Node(" + this.getId().toString() + ")";
		}

		@Override
		public int hashCode() {
			return getId().hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Node)) {
				return false;
			}

			return this.id.equals(((Node) other).id);
		}

		public TermId getId() {
			return id;
		}
	}

	public void validate() {
		if (!DEBUG)
			return;

		for (Node n : this.nodes.values()) {
			if (!this.children.containsKey(n)) {
				throw new RuntimeException("Missing children for node " + n.toString());
			}
			if (!this.parents.containsKey(n)) {
				throw new RuntimeException("Missing parents for node " + n.toString());
			}
		}
	}

	/*
	 * Graph Structure fields
	 */

	private HashMap<Node, Set<Node>> children = new HashMap<>();
	private HashMap<Node, Set<Node>> parents = new HashMap<>();
	private HashMap<TermId, Node> nodes = new HashMap<>();
	private Set<Node> roots = new HashSet<>();
	public Set<Node> leaves = new HashSet<>();
	private static boolean DEBUG = false;

	public Graph() {
	}

	public Graph(TermId rootId, ITerm termNode) {
		Node root = new Node(this, rootId);
		root.virtualTerm = termNode;
		this.roots.add(root);
		this.leaves.add(root);
		this.nodes.put(root.getId(), root);
		this.children.put(root, new HashSet<>());
		this.parents.put(root, new HashSet<>());
	}

	/*
	 * Getters
	 */

	public Node getNode(TermId n) {
		return this.nodes.get(n);
	}

	public Set<Node> childrenOf(Node n) {
		return this.children.get(n);
	}

	public Set<Node> parentsOf(Node n) {
		return this.parents.get(n);
	}

	public Collection<Node> nodes() {
		return this.nodes.values();
	}

	public Collection<Node> roots() {
		return this.roots;
	}

	public Collection<Node> leaves() {
		return this.leaves;
	}

	public long size() {
		return this.nodes.size();
	}

	/*
	 * Multi-graph Mutation
	 */

	/**
	 * Adds the given child relations to this graph, adding to existing collections
	 * if exists.
	 * 
	 * @param other
	 */
	private void mergeChildren(HashMap<Node, Set<Node>> other) {
		for (Entry<Node, Set<Node>> e : other.entrySet()) {
			Node newKey = e.getKey().withGraph(this);
			Set<Node> newNodes = e.getValue().stream().map(n -> n.withGraph(this)).collect(Collectors.toSet());

			this.children.putIfAbsent(newKey, new HashSet<>());
			this.children.get(newKey).addAll(newNodes);
		}
	}

	/**
	 * Adds the given parent relations to this graph, adding to existing collections
	 * if exists.
	 * 
	 * @param other
	 */
	private void mergeParents(HashMap<Node, Set<Node>> other) {
		for (Entry<Node, Set<Node>> e : other.entrySet()) {
			Node newKey = e.getKey().withGraph(this);
			Set<Node> newNodes = e.getValue().stream().map(n -> n.withGraph(this)).collect(Collectors.toSet());

			this.parents.putIfAbsent(newKey, new HashSet<>());
			this.parents.get(newKey).addAll(newNodes);
		}
	}

	/**
	 * Adds the nodes to this graph.
	 * 
	 * @param other
	 */
	private void mergeNodes(HashMap<TermId, Node> other) {
		for (Entry<TermId, Node> e : other.entrySet()) {
			this.nodes.put(e.getKey(), e.getValue().withGraph(this));
		}
	}

	/**
	 * Adds the leafs to this graph.
	 * 
	 * @param other
	 */
	private void mergeLeafs(Collection<Node> other) {
		for (Node n : other) {
			this.leaves.add(n.withGraph(this));
		}
	}

	/**
	 * Adds the roots to this graph.
	 * 
	 * @param other
	 */
	private void mergeRoots(Collection<Node> other) {
		for (Node n : other) {
			this.roots.add(n.withGraph(this));
		}
	}

	/**
	 * Places the graph <code>o</code> into this graph.
	 * 
	 * This adds the leaves and roots of <code>o</code> to those of this graph. Does
	 * not create any other edges.
	 * 
	 * @param o
	 */
	public void mergeGraph(Graph o) {
		this.mergeNodes(o.nodes);
		this.mergeChildren(o.children);
		this.mergeParents(o.parents);
		this.mergeLeafs(o.leaves());
		this.mergeRoots(o.roots());
	}

	/**
	 * Places the graph <code>o</code> into this graph, with edges between the roots
	 * of <code>o</code> and the given parents.
	 * 
	 * Does not change the roots and leaves of this graph, unless the parents
	 * collection is empty, in which case the graphs are merged as if
	 * mergeGraph(Graph) was called.
	 *
	 * @param parents
	 * @param o
	 */
	public void mergeGraph(Collection<Node> parents, Graph o) {
		if (o.size() == 0) {
			return;
		}

		if (parents.size() == 0) {
			this.mergeGraph(o);
		} else {
			this.mergeNodes(o.nodes);
			this.mergeChildren(o.children);
			this.mergeParents(o.parents);

			for (Node n : parents) {
				for (Node r : o.roots) {
					this.createEdge(n.withGraph(this), r.withGraph(this));
				}
			}
		}
	}

	/**
	 * Places the graph <code>o</code> into this graph, with edges between the roots
	 * of <code>o</code> and the given parents, and edges between the leaves of
	 * <code>o</code> and the given children.
	 * 
	 * Does not change the roots and leaves of this graph.
	 * 
	 * If <code>o</code> is empty, edges will be created between each parent and
	 * child in the given collections.
	 * 
	 * @param parents
	 * @param children
	 * @param o
	 */
	public void mergeGraph(Collection<Node> parents, Collection<Node> children, Graph o) {
		if (o.size() == 0) {
			for (Node p : parents) {
				for (Node c : children) {
					// p and c are of this graph so no update to their graph pointers necessary
					this.createEdge(p, c);
				}
			}
			return;
		}

		this.mergeNodes(o.nodes);
		this.mergeChildren(o.children);
		this.mergeParents(o.parents);

		for (Node n : parents) {
			for (Node r : o.roots) {
				this.createEdge(n, r.withGraph(this));
			}
		}

		for (Node n : children) {
			for (Node l : o.leaves) {
				this.createEdge(l.withGraph(this), n);
			}
		}
	}

	/*
	 * Graph Mutation
	 */

	/*
	 * Creates a non-ghost node without id, term, etc. Only use for tests.
	 */
	public Node createNode() {
		Node n = new Node(this);
		n.isGhost = false;
		this.addNode(n);
		return n;
	}

	public void addNode(Node n) {
		Node newNode = new Node(this, n);
		this.nodes.put(newNode.getId(), newNode);
		this.parents.put(newNode, new HashSet<>());
		this.children.put(newNode, new HashSet<>());
	}

	public void createEdge(Node parent, Node child) {
		this.children.get(parent).add(child);
		this.parents.get(child).add(parent);
	}

	public void removeNode(Node n) {
		if (this.roots.contains(n)) {
			this.roots.remove(n);

			for (Node child : this.children.get(n)) {
				this.roots.add(child);
			}
		}

		if (this.leaves.contains(n)) {
			this.leaves.remove(n);

			for (Node parent : this.parents.get(n)) {
				this.leaves.add(parent);
			}
		}

		for (Node child : this.children.get(n)) {
			this.parents.get(child).remove(n);
		}
		for (Node parent : this.parents.get(n)) {
			this.children.get(parent).remove(n);
		}
		for (Node child : this.children.get(n)) {
			for (Node parent : this.parents.get(n)) {
				this.createEdge(parent, child);
			}
		}

		this.children.remove(n);
		this.parents.remove(n);
		this.nodes.remove(n.getId());
	}

	private boolean unroot(Node n) {
		return this.roots.remove(n);
	}

	private boolean unleaf(Node n) {
		return this.leaves.remove(n);
	}

	/**
	 * Removes node n from the graph, removing all edges with it. In contrast to
	 * removeNode(Node) this will not create edges between children and parents of
	 * n. This will also not create new roots and leaves.
	 */
	private void removeNodeAndEdges(Node n) {
		this.unroot(n);
		this.unleaf(n);

		for (Node child : this.children.get(n)) {
			this.parents.get(child).remove(n);
		}
		for (Node parent : this.parents.get(n)) {
			this.children.get(parent).remove(n);
		}

		this.children.remove(n);
		this.parents.remove(n);
		this.nodes.remove(n.getId());
	}

	public void replaceNodes(Set<Node> nodes, Set<Node> predecessors, Set<Node> successors, Graph subGraph) {
		Flock.beginTime("Graph@replaceNodes");
		boolean containedRoot = nodes.stream().anyMatch(node -> this.roots.contains(node));
		boolean containedLeaf = nodes.stream().anyMatch(node -> this.leaves.contains(node));

		for (Node remove : nodes) {
			this.removeNodeAndEdges(remove);
		}

		// If root was replaced, then sub graph roots are also roots
		if (containedRoot) {
			this.roots.addAll(subGraph.roots);
		}

		// If leaf was replaced, then sub graph leaves are also leaves
		if (containedLeaf) {
			this.leaves.addAll(subGraph.leaves);
		}

		this.mergeGraph(predecessors, successors, subGraph);
		this.validate();

		Flock.endTime("Graph@replaceNodes");
	}

	public void removeGhostNodes() {
		Flock.beginTime("Graph@removeGhostNodes");
		Set<Node> ghostNodes = this.nodes().stream().filter(n -> n.isGhost).collect(Collectors.toSet());
		for (Node n : ghostNodes) {
			this.removeNode(n);
		}
		Flock.endTime("Graph@removeGhostNodes");
	}

	/*
	 * Helpers
	 */

	public String escapeString(String input) {
		return input.replace("\\", "\\\\").replace("\t", "\\t").replace("\b", "\\b").replace("\n", "\\n")
				.replace("\r", "\\r").replace("\f", "\\f").replace("\'", "\\'").replace("\"", "\\\"");
	}

	public String escapeBrackets(String input) {
		return input.replace("{", "\\{").replace("}", "\\}");
	}

	public String removeAnnotations(String input) {
		return input.replaceAll("\\{(.*?)\\}", "");
	}

	public String toGraphviz(String propertyName, boolean includeNodeType, boolean includeIntervals,
			boolean includeIds) {
		Flock.beginTime("graph@toGraphviz");
		StringBuilder result = new StringBuilder();
		result.append(
				"digraph G { graph[rankdir=LR, center=true, margin=0.2, nodesep=0.1, ranksep=0.3]; node[shape=record];");
		for (Node node : this.nodes.values()) {

			String termString = node.virtualTerm != null ? removeAnnotations(escapeString(node.virtualTerm.toString(1)))
					: "";

			String rootString = this.roots.contains(node) ? "r" : "";
			String leafString = this.leaves.contains(node) ? "l" : "";

			result.append(node.getId().getId() + "[label=\"");

			if (includeNodeType) {
				result.append("<f0>" + rootString + leafString);
			}

			// We need this to determine if we have to prepend '|'
			boolean printedAField = includeNodeType;

			if (includeIds) {
				if (printedAField) {
					result.append("|");
				}
				result.append("<f1>" + node.getId().getId());
				printedAField = true;
			}

			if (printedAField) {
				result.append("|");
			}
			printedAField = true;
			result.append("<f2>" + termString);

			if (propertyName != null) {
				Property h = node.properties.get(propertyName);
				if (h != null && h.lattice.value() != null) {
					if (printedAField) {
						result.append("|");
					}
					result.append("<f3>" + escapeBrackets(escapeString(h.lattice.value().toString())));
				}
			}

			result.append("\"];");

			for (Node child : this.childrenOf(node)) {
				result.append(node.getId().getId() + "->" + child.getId().getId() + ";");
			}
		}
		result.append("} ");
		Flock.endTime("graph@toGraphviz");
		return result.toString();
	}

	public String escapeStringSPT(String input) {
		return input.replace("\"", "\\\"");
	}

	// This does escaping slightly differently so the output can be used in spt
	// tests
	public String toGraphvizSPT(String propertyName, boolean includeNodeType, boolean includeIntervals,
			boolean includeIds) {
		Flock.beginTime("graph@toGraphviz");
		StringBuilder result = new StringBuilder();
		result.append(
				"digraph G { graph[rankdir=LR, center=true, margin=0.2, nodesep=0.1, ranksep=0.3]; node[shape=record];");
		for (Node node : this.nodes.values()) {

			String termString = node.virtualTerm != null
					? removeAnnotations(escapeStringSPT(escapeString(node.virtualTerm.toString(1))))
					: "";

			String rootString = this.roots.contains(node) ? "r" : "";
			String leafString = this.leaves.contains(node) ? "l" : "";

			result.append(node.getId().getId() + "[label=\\\"");

			if (includeNodeType) {
				result.append("<f0>" + rootString + leafString);
			}

			// We need this to determine if we have to prepend '|'
			boolean printedAField = includeNodeType;

			if (includeIds) {
				if (printedAField) {
					result.append("|");
				}
				result.append("<f1>" + node.getId().getId());
				printedAField = true;
			}

			if (printedAField) {
				result.append("|");
			}
			printedAField = true;
			result.append("<f2>" + termString);

			if (propertyName != null) {
				Property h = node.properties.get(propertyName);
				if (h != null && h.lattice.value() != null) {
					if (printedAField) {
						result.append("|");
					}
					result.append("<f3>" + escapeBrackets(escapeString(h.lattice.value().toString())));
				}
			}

			result.append("\\\"];");

			for (Node child : this.childrenOf(node)) {
				result.append(node.getId().getId() + "->" + child.getId().getId() + ";");
			}
		}
		result.append("} ");
		Flock.endTime("graph@toGraphviz");
		return result.toString();
	}

	public String toGraphviz(String property) {
		return toGraphviz(property, true, true, true);
	}

	public String toGraphviz() {
		return toGraphviz(null);
	}
}
