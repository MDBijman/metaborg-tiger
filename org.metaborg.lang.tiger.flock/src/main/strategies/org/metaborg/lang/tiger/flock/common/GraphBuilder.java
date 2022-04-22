package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;

public class GraphBuilder {
	public static class TermGraphMapping {

		public TermId ENTRY = Flock.nextNodeId();
		public TermId EXIT = Flock.nextNodeId();
		public TermId term;

		public TermGraphMapping(TermId n) {
			this.term = n;
		}
	}

	public final TermId START = Flock.nextNodeId();
	public final TermId END = Flock.nextNodeId();
	private HashSet<TermId> nodes = new HashSet<>();
	private HashMap<TermId, NodeType> types = new HashMap<>();
	private HashMap<TermId, Set<TermId>> children = new HashMap<>();
	private HashMap<TermId, Set<TermId>> parents = new HashMap<>();
	private HashMap<TermId, TermId> entry = new HashMap<>();
	private HashMap<TermId, TermId> exit = new HashMap<>();
	private HashSet<TermId> irregular = new HashSet<>();

	public GraphBuilder() {
		this.addNode(this.START, NodeType.START);
		this.addNode(this.END, NodeType.END);
	}

	public boolean isEmpty() {
		return this.nodes.size() == 4 && this.entry.size() == 1 && this.exit.size() == 1;
	}

	public static TermGraphMapping newMapping(TermId t) {
		TermGraphMapping r = new TermGraphMapping(t);
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
	
	public void materializeEntryExit(TermGraphMapping o) {
		this.addNode(o.ENTRY, NodeType.ENTRY);
		this.addNode(o.EXIT, NodeType.EXIT);

		this.entry.put(o.term, o.ENTRY);
		this.exit.put(o.term, o.EXIT);
	}
	
	public void materializeNode(TermGraphMapping o) {
		this.addNode(o.term, NodeType.NORMAL);
	}
	
//	public void materializeUnconnected(TermGraphMapping o) {
//		this.addNode(o.term, NodeType.NORMAL);
//		this.addNode(o.ENTRY, NodeType.ENTRY);
//		this.addNode(o.EXIT, NodeType.EXIT);
//
//		this.entry.put(o.term, o.ENTRY);
//		this.exit.put(o.term, o.EXIT);
//	}
//
//	public void materializeAsNode(TermGraphMapping o) {
//		this.addNode(o.term, NodeType.NORMAL);
//		this.addNode(o.ENTRY, NodeType.ENTRY);
//		this.addNode(o.EXIT, NodeType.EXIT);
//
//		this.entry.put(o.term, o.ENTRY);
//		this.exit.put(o.term, o.EXIT);
//		this.connect(o.ENTRY, o.term);
//		this.connect(o.term, o.EXIT);
//	}
//
//	public void materializeAsFallthrough(TermGraphMapping o) {
//		this.addNode(o.ENTRY, NodeType.ENTRY);
//		this.addNode(o.EXIT, NodeType.EXIT);
//
//		this.entry.put(o.term, o.ENTRY);
//		this.exit.put(o.term, o.EXIT);
//		this.connect(o.ENTRY, o.EXIT);
//	}

	public static Graph build(List<GraphBuilder> graphs, TermTree tree) {
		HashMap<TermId, Node> nodes = new HashMap<>();
		HashMap<TermId, TermId> entries = new HashMap<>();
		HashMap<TermId, TermId> exits = new HashMap<>();
		for (GraphBuilder builder : graphs) {
			entries.putAll(builder.entry);
			exits.putAll(builder.exit);
			// Fill nodes
			for (TermId t : builder.nodes) {
				nodes.put(t, new Node(null, t, tree.nodeById(t), builder.types.get(t)));
			}
		}

		Graph g = new Graph(nodes, entries, exits);

		for (GraphBuilder builder : graphs) {
			for (Node n : nodes.values()) {
				n.withGraph(g);
			}

			// Fill children
			for (Map.Entry<TermId, Set<TermId>> t : builder.children.entrySet()) {
				Set<Node> tChildren = new HashSet<>();
				for (TermId c : t.getValue()) {
					tChildren.add(nodes.get(c));
				}
				nodes.get(t.getKey()).children = tChildren;
			}

			// Fill parents
			for (Map.Entry<TermId, Set<TermId>> t : builder.parents.entrySet()) {
				Set<Node> tParents = new HashSet<>();
				for (TermId c : t.getValue()) {
					tParents.add(nodes.get(c));
				}
				nodes.get(t.getKey()).parents = tParents;
			}

			for (TermId t : builder.irregular) {
				tree.markIrregular(t);
			}
		}
		return g;
	}
}
