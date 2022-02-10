package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.Graph.Node;

public class GraphBuilder {
	private HashSet<TermId> nodes = new HashSet<>();
	private HashMap<TermId, Set<TermId>> children = new HashMap<>();
	private HashMap<TermId, Set<TermId>> parents = new HashMap<>();
	private Set<TermId> fromEntry = new HashSet<>();
	private Set<TermId> toExit = new HashSet<>();
	private Set<TermId> fromStart = new HashSet<>();
	private Set<TermId> toEnd = new HashSet<>();
	private Set<TermId> placeholders = new HashSet<>();

	private GraphBuilder() {
	}
	
	public static GraphBuilder empty() {
		GraphBuilder r = new GraphBuilder();
		return r;
	}

	public static GraphBuilder placeholder() {
		TermId tmp = Flock.nextNodeId();
		GraphBuilder r = GraphBuilder.fromSingle(tmp);
		r.placeholders.add(tmp);
		return r;
	}

	public static GraphBuilder fromSingle(TermId n) {
		GraphBuilder r = new GraphBuilder();
		r.nodes.add(n);
		r.children.put(n, new HashSet<>());
		r.parents.put(n, new HashSet<>());
		r.fromEntry.add(n);
		r.toExit.add(n);
		return r;
	}

	private boolean hasPlaceholders() {
		return this.placeholders.size() != 0;
	}

	public int size() {
		return this.nodes.size();
	}
	
	public Collection<TermId> nodes() {
		return this.nodes;
	}

	public void fromEntry(TermId n) {
		this.fromEntry.add(n);
	}

	public void fromEntry(Collection<TermId> n) {
		this.fromEntry.addAll(n);
	}

	public Collection<TermId> getEntry() {
		return this.fromEntry;
	}

	public void toExit(TermId n) {
		this.toExit.add(n);
	}

	public void toExit(Collection<TermId> n) {
		this.toExit.addAll(n);
	}

	public void setExit(Collection<TermId> n) {
		this.toExit.clear();
		this.toExit.addAll(n);
	}
	
	public void clearExit() {
		this.toExit.clear();
	}

	public Collection<TermId> getExit() {
		return this.toExit;
	}

	public void fromStart(TermId n) {
		this.fromStart.add(n);
	}

	public void fromStart(Collection<TermId> n) {
		this.fromStart.addAll(n);
	}

	public Collection<TermId> getStart() {
		return this.fromStart;
	}

	public void toEnd(TermId n) {
		this.toEnd.add(n);
	}

	public void toEnd(Collection<TermId> n) {
		this.toEnd.addAll(n);
	}

	public void clearEnd(Collection<TermId> n) {
		this.toEnd.clear();
	}
	
	public Collection<TermId> getEnd() {
		return this.toEnd;
	}

	public void merge(GraphBuilder o) {
		this.nodes.addAll(o.nodes);
		this.children.putAll(o.children);
		this.parents.putAll(o.parents);
		this.fromStart.addAll(o.fromStart);
		this.toEnd.addAll(o.toEnd);
		this.placeholders.addAll(o.placeholders);
	}

	public void connect(Collection<TermId> parents, Collection<TermId> children) {
		for (TermId p : parents) {
			this.children.get(p).addAll(children);
		}
		for (TermId c : children) {
			this.parents.get(c).addAll(parents);
		}
	}

	public Graph build(TermTree tree) {
		HashMap<TermId, Node> nodes = new HashMap<>();
		HashMap<Node, Set<Node>> children = new HashMap<>();
		HashMap<Node, Set<Node>> parents = new HashMap<>();
		HashSet<Node> roots = new HashSet<>();
		HashSet<Node> leafs = new HashSet<>();
		Graph g = new Graph(nodes, children, parents, roots, leafs);

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

		// Fill parents
		for (Map.Entry<TermId, Set<TermId>> t : this.parents.entrySet()) {
			Set<Node> tParents = new HashSet<>();
			for (TermId c : t.getValue()) {
				tParents.add(nodes.get(c));
			}
			parents.put(nodes.get(t.getKey()), tParents);
		}

		// Fill roots/leafs
		for (TermId t : this.fromEntry) {
			roots.add(nodes.get(t));
		}
		for (TermId t : this.fromStart) {
			roots.add(nodes.get(t));
		}
		for (TermId t : this.toExit) {
			leafs.add(nodes.get(t));
		}
		for (TermId t : this.toEnd) {
			leafs.add(nodes.get(t));
		}
		
		for (TermId placeholder : this.placeholders) {
			nodes.get(placeholder).isGhost = true;
		}

		return g;
	}
}
