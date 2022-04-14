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
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.graph.GraphFactory.PartialGraph;

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
		public Component component = null;
		ArrayList<Boolean> initialized = new ArrayList<>(Flock.instance.numberOfAnalyses());
		public Set<Node> parents = new HashSet<>();
		public Set<Node> children = new HashSet<>();

		private void fillInitialized() {
			for (int i = 0; i < Flock.instance.numberOfAnalyses(); i++) {
				this.initialized.add(false);
			}
		}

		private Node(NodeType t) {
			this.type = t;
			this.fillInitialized();
		}

		public Node(Graph parent, Node other, NodeType t) {
			this.type = t;
			this.id = other.id;
			this.properties = other.properties;
			this.virtualTerm = other.virtualTerm;
			this.graph = parent;
			this.fillInitialized();
		}

		public Node(Graph parent, TermId id, NodeType t) {
			this.type = t;
			this.id = id;
			this.graph = parent;
			this.fillInitialized();
		}

		public Node(Graph parent, TermId id, ITerm t, NodeType ty) {
			this.type = ty;
			this.id = id;
			this.virtualTerm = t;
			this.graph = parent;
			this.fillInitialized();
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
				return this.parents;
			} else {
				return this.children;
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
	}

	/*
	 * Graph Structure fields
	 */

	private HashMap<TermId, Node> nodes = new HashMap<>();

	private HashMap<TermId, TermId> entry = new HashMap<>();
	private HashMap<TermId, TermId> exit = new HashMap<>();
	private HashSet<TermId> starts = new HashSet<>();
	private HashSet<TermId> ends = new HashSet<>();

	private static final boolean DEBUG = Flock.DEBUG;

	public Graph() {
	}

	public Graph(HashMap<TermId, Node> nodes, HashMap<TermId, TermId> entry, HashMap<TermId, TermId> exit) {
		this.nodes = nodes;
		this.entry = entry;
		this.exit = exit;

		for (Node n : nodes.values()) {
			if (n.type == NodeType.START)
				this.starts.add(n.id);
			else if (n.type == NodeType.END)
				this.ends.add(n.id);
		}
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

	public Collection<Node> nodes() {
		return this.nodes.values();
	}

	public HashSet<TermId> starts() {
		return this.starts;
	}

	public HashSet<TermId> ends() {
		return this.ends;
	}

	public long size() {
		return this.nodes.size();
	}

	/*
	 * Multi-graph Mutation
	 */

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

		this.entry.putAll(o.entry);
		this.exit.putAll(o.exit);
		this.starts.addAll(o.starts);
		this.ends.addAll(o.ends);
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
	}

	public void createEdge(Node parent, Node child) {
		parent.children.add(child);
		child.parents.add(parent);
	}

	public void removeNodeAndConnectNeighbours(Node n) {
		for (Node child : n.children) {
			child.parents.remove(n);
		}
		for (Node parent : n.parents) {
			parent.children.remove(n);
		}
		for (Node child : n.children) {
			for (Node parent : n.parents) {
				this.createEdge(parent, child);
			}
		}

		this.nodes.remove(n.getId());
	}

	/**
	 * Removes node n from the graph, removing all edges with it.
	 */
	public void removeNode(Node n) {

		for (Node child : n.children) {
			child.parents.remove(n);
		}
		for (Node parent : n.parents) {
			parent.children.remove(n);
		}

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

		for (Node pred : this.nodes.get(entry).parents) {
			for (Node succ : this.nodes.get(exit).children) {
				this.createEdge(pred, succ);
			}
		}

		this.removeTerm(root);

		this.validate();

		Flock.endTime("Graph@removeNodes");
	}

	public void replaceNodes(TermId root, Set<TermId> innerTerms, PartialGraph newGraph) {
		Flock.beginTime("Graph@replaceNodes");

		// First put all elements of newGraph into this graph
		this.mergeGraph(newGraph.graph);

		TermId entry = this.entry.get(root);
		TermId exit = this.exit.get(root);

		// Create edges between the parents of `root` and the entry of newGraph
		Set<Node> parents = this.nodes.get(entry).parents;
		for (Node n : parents) {
			this.createEdge(n, this.nodes.get(newGraph.entry));
		}

		// Create edges between the children of `root` and the exit of newGraph
		Set<Node> children = this.nodes.get(exit).children;
		for (Node n : children) {
			this.createEdge(this.nodes.get(newGraph.exit), n);
		}

		// // If we delete the entry node, `newGraphs` entry is the new entry
//		if (this.theEntry.equals(this.getNode(entry))) {
//			this.theEntry = newGraph.theEntry.withGraph(this);
//		}
//
//		// If we delete the exit node, `newGraphs` exit is the new exit
//		if (this.theExit.equals(this.getNode(exit))) {
//			this.theExit = newGraph.theExit.withGraph(this);
//		}

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

			for (Node child : node.children) {
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

			for (Node child : node.children) {
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
