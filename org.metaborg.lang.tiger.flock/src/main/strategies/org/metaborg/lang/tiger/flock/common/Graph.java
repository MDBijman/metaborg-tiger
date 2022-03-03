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

import org.apache.commons.lang3.tuple.Pair;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;

public class Graph {
	public static class Node {
		private TermId id;

		public enum NodeType {
			START, END, ENTRY, EXIT, NORMAL
		};

		public NodeType type;
		public ITerm virtualTerm = null;
		public HashMap<String, Property> properties = new HashMap<>();
		public Graph graph = null;

		private Node(NodeType t) {
			this.type = t;
		}

		public Node(Graph parent, Node other, NodeType t) {
			this.type = t;
			this.id = other.id;
			this.properties = other.properties;
			this.virtualTerm = other.virtualTerm;
			this.graph = parent;
		}

		public Node(Graph parent, TermId id, NodeType t) {
			this.type = t;
			this.id = id;
			this.graph = parent;
		}

		public Node(Graph parent, TermId id, ITerm t, NodeType ty) {
			this.type = ty;
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

		public Collection<Node> predecessors(SingleAnalysis.Direction dir) {
			if (dir == SingleAnalysis.Direction.FORWARD) {
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

	private HashMap<TermId, TermId> entry = new HashMap<>();
	private HashMap<TermId, TermId> exit = new HashMap<>();
	private Node theEntry = null;
	private Node theExit = null;
	private Node theStart = null;
	private Node theEnd = null;

	private static final boolean DEBUG = Flock.DEBUG;

	public Graph() {
	}

	public Graph(HashMap<TermId, Node> nodes, HashMap<Node, Set<Node>> children, HashMap<Node, Set<Node>> parents,
			HashMap<TermId, TermId> entry, HashMap<TermId, TermId> exit, Node theEntry, Node theExit, Node theStart,
			Node theEnd) {
		this.nodes = nodes;
		this.children = children;
		this.parents = parents;
		this.entry = entry;
		this.exit = exit;
		this.theEntry = theEntry;
		this.theExit = theExit;
		this.theStart = theStart;
		this.theEnd = theEnd;
	}

	/*
	 * Getters
	 */

	public Node getNode(TermId n) {
		return this.nodes.get(n);
	}

	public TermId entryOf(TermId n) {
		return this.entry.get(n);
	}
	
	public TermId exitOf(TermId n) {
		return this.exit.get(n);
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

	public Node root() {
		return this.getStart();
	}

	public Node leaf() {
		return this.theEnd;
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
	 * Places the graph <code>o</code> into this graph. Does not change the roots
	 * and leaves of this graph.
	 * 
	 * @param o
	 */
	public void mergeGraph(Graph o) {
		this.mergeNodes(o.nodes);
		this.mergeChildren(o.children);
		this.mergeParents(o.parents);

		for (Node child : this.childrenOf(o.theStart.withGraph(this))) {
			this.createEdge(this.theStart, child);
		}

		for (Node parent : this.parentsOf(o.theEnd.withGraph(this))) {
			this.createEdge(parent, this.theEnd);
		}

		this.removeNode(o.theStart.withGraph(this));
		this.removeNode(o.theEnd.withGraph(this));

		this.entry.putAll(o.entry);
		this.exit.putAll(o.exit);
	}

	/*
	 * Graph Mutation
	 */

	/*
	 * Creates a non-transient node without id, term, etc. Only use for tests.
	 */
	public Node createNode() {
		Node n = new Node(NodeType.NORMAL);
		this.addNode(n);
		return n;
	}

	public void addNode(Node n) {
		Node newNode = new Node(this, n, NodeType.NORMAL);
		this.nodes.put(newNode.getId(), newNode);
		this.parents.put(newNode, new HashSet<>());
		this.children.put(newNode, new HashSet<>());
	}

	public void createEdge(Node parent, Node child) {
		this.children.get(parent).add(child);
		this.parents.get(child).add(parent);
	}

	public void removeNodeAndConnectNeighbours(Node n) {
		if (this.theStart.equals(n))
			throw new RuntimeException("Cannot remove start");
		if (this.theEnd.equals(n))
			throw new RuntimeException("Cannot remove end");
		if (this.theEntry.equals(n))
			throw new RuntimeException("Cannot remove entry");
		if (this.theExit.equals(n))
			throw new RuntimeException("Cannot remove exit");

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

	/**
	 * Removes node n from the graph, removing all edges with it.
	 */
	public void removeNode(Node n) {
		if (this.theStart.equals(n))
			throw new RuntimeException("Cannot remove start");
		if (this.theEnd.equals(n))
			throw new RuntimeException("Cannot remove end");
		if (this.theEntry.equals(n))
			throw new RuntimeException("Cannot remove entry");
		if (this.theExit.equals(n))
			throw new RuntimeException("Cannot remove exit");

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

	private void removeTerm(TermId n) {
		TermId entry = this.entry.get(n);
		TermId exit = this.exit.get(n);
		if (this.nodes.get(n) != null)
			this.removeNode(this.nodes.get(n));
		if (entry != null) {
			this.removeNode(this.nodes.get(entry));
			this.entry.remove(n);
		}
		if (exit != null) {
			this.removeNode(this.nodes.get(exit));
			this.exit.remove(n);
		}
	}

	public void removeNodes(TermId root, Set<TermId> innerTerms) {
		Flock.beginTime("Graph@removeNodes");

		TermId entry = this.entry.get(root);
		TermId exit = this.exit.get(root);

		for (TermId remove : innerTerms) {
			this.removeTerm(remove);
		}

		for (Node pred : this.parents.get(this.nodes.get(entry))) {
			for (Node succ : this.children.get(this.nodes.get(exit))) {
				this.createEdge(pred, succ);
			}
		}

		this.removeTerm(root);

		this.validate();

		Flock.endTime("Graph@removeNodes");
	}

	public void replaceNodes(TermId root, Set<TermId> innerTerms, Graph newGraph) {
		Flock.beginTime("Graph@replaceNodes");

		// First put all elements of newGraph into this graph
		this.mergeGraph(newGraph);

		TermId entry = this.entry.get(root);
		TermId exit = this.exit.get(root);

		// Create edges between the parents of `root` and the entry of newGraph
		Set<Node> parents = this.parents.get(this.nodes.get(entry));
		for (Node n : parents) {
			this.createEdge(n, this.nodes.get(newGraph.theEntry.id));
		}

		// Create edges between the children of `root` and the exit of newGraph
		Set<Node> children = this.children.get(this.nodes.get(exit));
		for (Node n : children) {
			this.createEdge(this.nodes.get(newGraph.theExit.id), n);
		}
		
		// If we delete the entry node, `newGraphs` entry is the new entry
		if (this.theEntry.equals(this.getNode(entry))) {
			this.theEntry = newGraph.theEntry.withGraph(this);
		}

		// If we delete the exit node, `newGraphs` exit is the new exit
		if (this.theExit.equals(this.getNode(exit))) {
			this.theExit = newGraph.theExit.withGraph(this);
		}

		// Remove all of the inner nodes
		for (TermId remove : innerTerms) {
			this.removeTerm(remove);
		}

		this.removeTerm(root);

		this.validate();

		Flock.endTime("Graph@replaceNodes");
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

			String typeString = node.type.toString();
			String originString = node.type.equals(NodeType.NORMAL)
					? this.entry.get(node.id).getId() + "," + this.exit.get(node.id).getId() + ","

					: "";

			result.append(node.getId().getId() + "[label=\"");

			if (includeNodeType) {
				result.append("<f0>" + typeString + "(" + originString + ")");
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

			String typeString = node.type.toString();

			result.append(node.getId().getId() + "[label=\\\"");

			if (includeNodeType) {
				result.append("<f0>" + typeString);
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

	public Node getStart() {
		return theStart;
	}

	public Node getEnd() {
		return theEnd;
	}
	
	public Node getEntry() {
		return theEntry;
	}

	public Node getExit() {
		return theExit;
	}
}
