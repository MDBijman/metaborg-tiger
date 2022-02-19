package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.metaborg.lang.tiger.flock.common.Graph.Node;

public class GraphBuilder {
	public final TermId START = Flock.nextNodeId();
	public final TermId END = Flock.nextNodeId();
	public final TermId ENTRY = Flock.nextNodeId();
	public final TermId EXIT = Flock.nextNodeId();
	private HashSet<TermId> nodes = new HashSet<>();
	private HashMap<TermId, Set<TermId>> children = new HashMap<>();
	private HashMap<TermId, Set<TermId>> parents = new HashMap<>();
	private HashSet<TermId> tmpNodes = new HashSet<>();

	private HashSet<TermId> irregularTerms = new HashSet<>();

	private class InactiveEdge {
		TermId target;
		TermId irregularOrigin;

		public InactiveEdge(TermId target, TermId irregularOrigin) {
			this.target = target;
			this.irregularOrigin = irregularOrigin;
		}
	}

	private HashMap<TermId, Set<InactiveEdge>> inactivePredecessors = new HashMap<>();
	private HashMap<TermId, Set<InactiveEdge>> inactiveSuccessors = new HashMap<>();

	private GraphBuilder() {
	}

	public static GraphBuilder empty() {
		GraphBuilder r = new GraphBuilder();
		r.addNode(r.START);
		r.addNode(r.END);
		r.addNode(r.ENTRY);
		r.addNode(r.EXIT);
		r.tmpNodes.add(r.START);
		r.tmpNodes.add(r.END);
		r.tmpNodes.add(r.ENTRY);
		r.tmpNodes.add(r.EXIT);
		return r;
	}

	public static GraphBuilder fallthrough() {
		GraphBuilder r = GraphBuilder.empty();
		r.connect(r.ENTRY, r.EXIT);
		return r;
	}

	public static GraphBuilder fromSingle(TermId n) {
		GraphBuilder r = GraphBuilder.empty();
		r.addNode(n);
		r.connect(r.ENTRY, n);
		r.connect(n, r.EXIT);
		return r;
	}

	private void addNode(TermId t) {
		this.nodes.add(t);
		this.children.put(t, new HashSet<>());
		this.parents.put(t, new HashSet<>());
	}

	public void connect(TermId parent, TermId child) {
		this.children.get(parent).add(child);
		this.parents.get(child).add(parent);
	}

	public boolean hasRealNodes() {
		return this.nodes.size() > this.tmpNodes.size();
	}

	public Collection<TermId> nodes() {
		return this.nodes;
	}

	public void merge(GraphBuilder o) {
		this.nodes.addAll(o.nodes);
		this.children.putAll(o.children);
		this.parents.putAll(o.parents);
		this.tmpNodes.addAll(o.tmpNodes);
		this.irregularTerms.addAll(o.irregularTerms);
		this.inactivePredecessors.putAll(o.inactivePredecessors);
		this.inactiveSuccessors.putAll(o.inactiveSuccessors);

		this.children.get(this.START).add(o.START);
		this.parents.get(o.START).add(this.START);
		this.children.get(o.END).add(this.END);
		this.parents.get(this.END).add(o.END);
	}

	public void markIrregular(TermId node) {
		this.irregularTerms.add(node);
		this.inactiveSuccessors.putIfAbsent(this.ENTRY, new HashSet<>());
		this.inactiveSuccessors.get(this.ENTRY).add(new InactiveEdge(this.EXIT, node));
		this.inactivePredecessors.putIfAbsent(this.EXIT, new HashSet<>());
		this.inactivePredecessors.get(this.EXIT).add(new InactiveEdge(this.ENTRY, node));
	}

	public Graph build(TermTree tree) {
		HashMap<TermId, Node> nodes = new HashMap<>();
		HashMap<Node, Set<Node>> children = new HashMap<>();
		HashMap<Node, Set<Node>> inactiveChildren = new HashMap<>();
		HashMap<TermId, Pair<Node, Node>> sourceMap = new HashMap<>();
		HashMap<Pair<Node, Node>, TermId> sourceMapRev = new HashMap<>();
		HashMap<Node, Set<Node>> parents = new HashMap<>();
		HashMap<Node, Set<Node>> inactiveParents = new HashMap<>();
		HashSet<Node> roots = new HashSet<>();
		HashSet<Node> leafs = new HashSet<>();
		Graph g = new Graph(nodes, children, parents, inactiveChildren, inactiveParents, sourceMap, sourceMapRev, roots,
				leafs);

		// Fill nodes
		for (TermId t : this.nodes) {
			nodes.put(t, new Node(g, t, tree.nodeById(t)));
		}

		// Fill children
		for (Map.Entry<TermId, Set<TermId>> t : this.children.entrySet()) {
			Set<Node> tChildren = new HashSet<>();
			for (TermId c : t.getValue()) {
				tChildren.add(nodes.get(c));
			}
			children.put(nodes.get(t.getKey()), tChildren);
		}

		// Fill inactive children
		for (Map.Entry<TermId, Set<InactiveEdge>> t : this.inactiveSuccessors.entrySet()) {
			Set<Node> tChildren = new HashSet<>();
			for (InactiveEdge c : t.getValue()) {
				tChildren.add(nodes.get(c.target));
				sourceMap.put(c.irregularOrigin, Pair.of(nodes.get(t.getKey()), nodes.get(c.target)));
				sourceMapRev.put(Pair.of(nodes.get(t.getKey()), nodes.get(c.target)), c.irregularOrigin);
			}
			inactiveChildren.put(nodes.get(t.getKey()), tChildren);
		}

		// Fill parents
		for (Map.Entry<TermId, Set<TermId>> t : this.parents.entrySet()) {
			Set<Node> tParents = new HashSet<>();
			for (TermId c : t.getValue()) {
				tParents.add(nodes.get(c));
			}
			parents.put(nodes.get(t.getKey()), tParents);
		}

		// Fill inactive parents
		for (Map.Entry<TermId, Set<InactiveEdge>> t : this.inactivePredecessors.entrySet()) {
			Set<Node> tParents = new HashSet<>();
			for (InactiveEdge c : t.getValue()) {
				tParents.add(nodes.get(c.target));
			}
			inactiveParents.put(nodes.get(t.getKey()), tParents);
		}

		// Fill roots/leafs
		for (TermId t : this.children.get(START)) {
			roots.add(nodes.get(t));
		}
		for (TermId t : this.parents.get(END)) {
			leafs.add(nodes.get(t));
		}
		for (TermId t : this.children.get(ENTRY)) {
			roots.add(nodes.get(t));
		}
		for (TermId t : this.parents.get(EXIT)) {
			leafs.add(nodes.get(t));
		}

		for (TermId t : this.tmpNodes) {
			nodes.get(t).isTransient = true;
		}

		for (TermId t : this.irregularTerms) {
			tree.markIrregular(t);
		}

		return g;
	}
}
