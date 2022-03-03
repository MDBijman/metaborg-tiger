package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;

public class GraphBuilder {
	public final TermId START = Flock.nextNodeId();
	public final TermId END = Flock.nextNodeId();
	public TermId ENTRY = Flock.nextNodeId();
	public TermId EXIT = Flock.nextNodeId();
	private HashSet<TermId> nodes = new HashSet<>();
	private HashMap<TermId, NodeType> types = new HashMap<>();
	private HashMap<TermId, Set<TermId>> children = new HashMap<>();
	private HashMap<TermId, Set<TermId>> parents = new HashMap<>();
	private HashMap<TermId, TermId> entry = new HashMap<>();
	private HashMap<TermId, TermId> exit = new HashMap<>();
	private HashSet<TermId> irregular = new HashSet<>();

	private GraphBuilder() {
	}

	public static GraphBuilder empty(TermId t) {
		GraphBuilder r = new GraphBuilder();
		r.addNode(r.START, NodeType.START);
		r.addNode(r.END, NodeType.END);
		r.addNode(r.ENTRY, NodeType.ENTRY);
		r.addNode(r.EXIT, NodeType.EXIT);
		r.entry.put(t, r.ENTRY);
		r.exit.put(t, r.EXIT);
		return r;
	}

	public static GraphBuilder fallthrough(TermId t) {
		GraphBuilder r = GraphBuilder.empty(t);
		r.connect(r.ENTRY, r.EXIT);
		return r;
	}

	public static GraphBuilder fromSingle(TermId n) {
		GraphBuilder r = new GraphBuilder();
		r.ENTRY = n;
		r.EXIT = n;
		r.addNode(r.START, NodeType.START);
		r.addNode(r.END, NodeType.END);
		r.addNode(r.ENTRY, NodeType.ENTRY);
		r.addNode(r.EXIT, NodeType.EXIT);
		r.addNode(n, NodeType.NORMAL);
		return r;
	}
	
	public void markIrregular(TermId n) {
		this.irregular.add(n);
	}

	private void addNode(TermId t, NodeType ty) {
		this.types.put(t, ty);
		this.nodes.add(t);
		this.children.put(t, new HashSet<>());
		this.parents.put(t, new HashSet<>());
	}

	private void deleteNode(TermId t) {
		this.types.remove(t);
		this.nodes.remove(t);
		Set<TermId> children = this.children.remove(t);
		for (TermId child : children)
			this.parents.get(child).remove(t);
		Set<TermId> parents = this.parents.remove(t);
		for (TermId parent : parents)
			this.children.get(parent).remove(t);
		this.entry.remove(t);
		this.exit.remove(t);
	}
	
	public void connect(TermId parent, TermId child) {
		this.children.get(parent).add(child);
		this.parents.get(child).add(parent);
	}

	public Collection<TermId> nodes() {
		return this.nodes;
	}

	public void merge(GraphBuilder o) {
		this.nodes.addAll(o.nodes);
		this.children.putAll(o.children);
		this.parents.putAll(o.parents);
		this.types.putAll(o.types);
		
		this.entry.putAll(o.entry);
		this.exit.putAll(o.exit);
		
		for (TermId startTerm : this.children.get(o.START)) {
			this.parents.get(startTerm).remove(o.START);
			this.connect(this.START, startTerm);
		}
		
		for (TermId endTerm : this.children.get(o.END)) {
			this.children.get(endTerm).remove(o.END);
			this.connect(endTerm, this.END);
		}
		
		this.deleteNode(o.START);
		this.deleteNode(o.END);
	}

	public Graph build(TermTree tree) {
		HashMap<TermId, Node> nodes = new HashMap<>();
		HashMap<Node, Set<Node>> children = new HashMap<>();
		HashMap<Node, Set<Node>> parents = new HashMap<>();
		
		// Fill nodes
		for (TermId t : this.nodes) {
			nodes.put(t, new Node(null, t, tree.nodeById(t), types.get(t)));
		}
		
		Node start = nodes.get(START);
		Node end = nodes.get(END);
		Node entry = nodes.get(ENTRY);
		Node exit = nodes.get(EXIT);

		Graph g = new Graph(nodes, children, parents, this.entry, this.exit, entry, exit, start, end);

		for (Node n : nodes.values()) {
			n.withGraph(g);
		}
		
		// Fill children
		for (Map.Entry<TermId, Set<TermId>> t : this.children.entrySet()) {
			Set<Node> tChildren = new HashSet<>();
			for (TermId c : t.getValue()) {
				if(nodes.get(c) == null) {
					Flock.printDebug("h");
				}
				tChildren.add(nodes.get(c));
			}
			children.put(nodes.get(t.getKey()), tChildren);
		}

		// Fill parents
		for (Map.Entry<TermId, Set<TermId>> t : this.parents.entrySet()) {
			Set<Node> tParents = new HashSet<>();
			for (TermId c : t.getValue()) {
				tParents.add(nodes.get(c));
			}
			parents.put(nodes.get(t.getKey()), tParents);
		}
		
		for (TermId t : this.irregular) {
			tree.markIrregular(t);
		}
		
		return g;
	}
}
