package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class Graph {
	public static class Node {
		private TermId id;

		public boolean isGhost = false;
		public ITerm virtualTerm = null;
		public float interval = 0.0f;
		public HashMap<String, Property> properties = new HashMap<>();

		public Node() {
			this.isGhost = true;
			this.id = Flock.nextNodeId();
		}

		public Node(TermId id) {
			this.id = id;
		}

		public Node(TermId id, ITerm t) {
			this.id = id;
			this.virtualTerm = t;
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
		for (Node n : this.nodes.values()) {
			if (!this.nodeInterval.containsKey(n)) {
				throw new RuntimeException("Missing interval for node " + n.toString());
			}
			if (!n.isGhost && !this.nodeTerm.containsKey(n)) {
				throw new RuntimeException("Missing term for node " + n.toString());
			}
			if (!this.children.containsKey(n)) {
				throw new RuntimeException("Missing children for node " + n.toString());
			}
			if (!this.parents.containsKey(n)) {
				throw new RuntimeException("Missing parents for node " + n.toString());
			}
			for (Node c : this.children.get(n)) {
				if (c.interval < n.interval) {
					throw new RuntimeException("child interval smaller than parent interval " + n.toString()
							+ n.interval + " - " + c.toString() + c.interval);
				}
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

	public Graph() {
		/*
		 * Node root = new Node(); this.roots.add(root); this.leaves.add(root);
		 * this.nodes.put(root.getId(), root); this.children.put(root, new HashSet<>());
		 * this.parents.put(root, new HashSet<>());
		 */
	}

	public Graph(Node root, ITerm termNode) {
		root.virtualTerm = termNode;
		this.roots.add(root);
		this.leaves.add(root);
		this.nodes.put(root.getId(), root);
		this.children.put(root, new HashSet<>());
		this.parents.put(root, new HashSet<>());
		this.nodeTerm.put(root, termNode);
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

	private void mergeChildren(HashMap<Node, Set<Node>> other) {
		for (Entry<Node, Set<Node>> e : other.entrySet()) {
			if (this.children.containsKey(e.getKey())) {
				this.children.get(e.getKey()).addAll(e.getValue());
			} else {
				this.children.put(e.getKey(), e.getValue());
			}
		}
	}

	private void mergeParents(HashMap<Node, Set<Node>> other) {
		for (Entry<Node, Set<Node>> e : other.entrySet()) {
			if (this.parents.containsKey(e.getKey())) {
				this.parents.get(e.getKey()).addAll(e.getValue());
			} else {
				this.parents.put(e.getKey(), e.getValue());
			}
		}
	}

	public void mergeGraph(Graph o) {
		this.nodes.putAll(o.nodes);
		this.mergeChildren(o.children);
		this.mergeParents(o.parents);
		this.nodeInterval.putAll(o.nodeInterval);
		this.nodeTerm.putAll(o.nodeTerm);
		this.leaves.addAll(o.leaves());
		this.roots.addAll(o.roots());
	}

	public void attachChildGraph(Collection<Node> parents, Graph o) {
		if (o.size() == 0) {
			return;
		}

		if (parents.size() == 0) {
			this.mergeGraph(o);
		} else {
			this.nodes.putAll(o.nodes);
			this.mergeChildren(o.children);
			this.mergeParents(o.parents);
			this.nodeInterval.putAll(o.nodeInterval);
			this.nodeTerm.putAll(o.nodeTerm);

			for (Node n : parents) {
				for (Node r : o.roots) {
					this.createEdge(n, r);
				}
			}
		}
	}

	public void mergeGraph(Collection<Node> parents, Collection<Node> children, Graph o) {
		if (o.size() == 0) {
			for (Node p : parents) {
				for (Node c : children) {
					this.createEdge(p, c);
				}
			}
			return;
		}

		this.nodes.putAll(o.nodes);
		this.mergeChildren(o.children);
		this.mergeParents(o.parents);
		this.nodeInterval.putAll(o.nodeInterval);
		this.nodeTerm.putAll(o.nodeTerm);

		for (Node n : parents) {
			for (Node r : o.roots) {
				this.createEdge(n, r);
			}
		}

		for (Node n : children) {
			for (Node r : o.leaves) {
				this.createEdge(r, n);
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
		Node n = new Node();
		n.isGhost = false;
		this.addNode(n);
		return n;
	}

	public void addNode(Node n) {
		this.nodes.put(n.getId(), n);
		this.parents.put(n, new HashSet<>());
		this.children.put(n, new HashSet<>());
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

	/*
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

	public void replaceNodes(Set<Node> n, Graph subGraph) {
		Flock.beginTime("Graph@replaceNodes");
		Set<Node> oldParents = new HashSet<>();
		Set<Node> oldChildren = new HashSet<>();

		boolean containedRoot = n.stream().anyMatch(node -> this.roots.contains(node));
		boolean containedLeaf = n.stream().anyMatch(node -> this.leaves.contains(node));

		// Remove the nodes, and remember the nodes that were connected to the removed
		// set
		for (Node remove : n) {
			oldParents.remove(remove);
			oldChildren.remove(remove);
			oldParents.addAll(this.parentsOf(remove));
			oldChildren.addAll(this.childrenOf(remove));

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
		this.mergeGraph(oldParents, oldChildren, subGraph);
		this.updateIntervals(subGraph.nodes.values(), oldParents, oldChildren);
		// this.validate();
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
	 * Terms
	 */

	private HashMap<Node, ITerm> nodeTerm = new HashMap<>();

	public ITerm termOf(Node n) {
		return this.nodeTerm.get(n);
	}

	/*
	 * Intervals
	 */

	private HashMap<Node, Float> nodeInterval = new HashMap<>();

	public Float intervalOf(Node n) {
		return this.nodeInterval.get(n);
	}

	private static final float VERY_LARGE_FLOAT = 999.f;

	private void updateIntervals(Collection<Node> newNodes, Set<Node> oldParents, Set<Node> oldChildren) {
		Flock.beginTime("Graph@updateIntervals");
		if (newNodes.size() == 0) {
			return;
		}

		float parentMax = oldParents.stream().map(p -> this.intervalOf(p)).max(Float::compare).orElse(0.f);
		float childMin = oldChildren.stream().map(p -> this.intervalOf(p)).min(Float::compare).orElse(VERY_LARGE_FLOAT);

		// Check if matching, then all newNodes get the common interval
		// If not, take the difference between min of parents and max of children, put
		// newNodes intervals in between as floats

		assert parentMax <= childMin;

		if (parentMax == childMin) {
			for (Node n : newNodes) {
				this.nodeInterval.put(n, parentMax);
			}
		} else {
			float diff = childMin - parentMax;

			float newMax = newNodes.stream().map(p -> this.intervalOf(p)).max(Float::compare).get();

			for (Node n : newNodes) {
				float currentInterval = this.intervalOf(n);
				float newInterval = parentMax + (currentInterval / (newMax + 1)) * diff;
				this.nodeInterval.put(n, newInterval);
				n.interval = newInterval;
			}
		}
		Flock.endTime("Graph@updateIntervals");
	}

	public void computeIntervals() {
		Flock.beginTime("Graph@computeIntervals");
		this.nodeInterval.clear();

		// Using AtomicLong to pass mutable reference
		AtomicLong next_index = new AtomicLong(1);
		Stack<Node> S = new Stack<>();
		HashSet<Node> onStack = new HashSet<>();
		HashMap<Node, Long> lowlink = new HashMap<>();
		HashMap<Node, Long> index = new HashMap<>();
		Set<Set<Node>> components = new HashSet<>();
		HashMap<Node, Set<Node>> nodeComponent = new HashMap<>();

		for (Node n : this.roots) {
			if (index.get(n) == null) {
				strongConnect(n, next_index, S, onStack, lowlink, index, components, nodeComponent);
			}
		}

		// This can be optimized much better
		// Kahn's algorithm applied to the scc's of the graph

		// Setup the edges between the scc's
		// Map from SCC to all successor SCC's
		HashMap<Set<Node>, Set<Set<Node>>> successors = new HashMap<>();
		HashMap<Set<Node>, Set<Set<Node>>> predecessors = new HashMap<>();
		for (Set<Node> set : components) {
			successors.put(set, new HashSet<>());
			predecessors.put(set, new HashSet<>());
		}

		for (Node n : this.nodes.values()) {
			for (Node c : this.childrenOf(n)) {
				Set<Node> nComponent = nodeComponent.get(n);
				Set<Node> cComponent = nodeComponent.get(c);
				if (nComponent != cComponent) {
					successors.get(nComponent).add(cComponent);
					predecessors.get(cComponent).add(nComponent);
				}
			}
		}

		Set<Set<Node>> noOutgoing = new HashSet<>();

		// Find component with no successors (i.e. no outgoing edge)
		for (Set<Node> component : components) {
			if (successors.get(component).isEmpty()) {
				noOutgoing.add(component);
			}
		}

		long currIndex = components.size() + 1;
		while (!components.isEmpty()) {
			Set<Node> c = noOutgoing.iterator().next();
			noOutgoing.remove(c);

			// Remove component and set interval of its nodes
			components.remove(c);
			for (Set<Node> p : predecessors.get(c)) {
				successors.get(p).remove(c);
				if (successors.get(p).size() == 0)
					noOutgoing.add(p);
			}

			successors.remove(c);
			predecessors.remove(c);

			for (Node n : c) {
				this.nodeInterval.put(n, (float) currIndex);
				n.interval = (float) currIndex;
			}
			currIndex--;
		}
		Flock.endTime("Graph@computeIntervals");
	}

	// Tarjans
	private void strongConnect(Node v, AtomicLong next_index, Stack<Node> S, HashSet<Node> onStack,
			HashMap<Node, Long> lowlink, HashMap<Node, Long> index, Set<Set<Node>> components,
			HashMap<Node, Set<Node>> nodeComponent) {
		index.put(v, next_index.longValue());
		lowlink.put(v, next_index.longValue());
		next_index.addAndGet(1);
		S.push(v);
		onStack.add(v);
		for (Node w : this.childrenOf(v)) {
			if (index.get(w) == null) {
				strongConnect(w, next_index, S, onStack, lowlink, index, components, nodeComponent);
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
		if (lowlink.get(v) == index.get(v)) {
			Node w;
			HashSet<Node> component = new HashSet<>();
			do {
				w = S.pop();
				onStack.remove(w);
				nodeComponent.put(w, component);
				component.add(w);
			} while (w != v);
			components.add(component);
		}
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

			String termString = node.virtualTerm != null ? removeAnnotations(escapeString(node.virtualTerm.toString(1))) : "";

			String rootString = this.roots.contains(node) ? "r" : "";
			String leafString = this.leaves.contains(node) ? "l" : "";
			String intervalString = this.nodeInterval.containsKey(node) ? this.intervalOf(node).toString() : "";

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

			if (includeIntervals) {
				if (printedAField) {
					result.append("|");
				}
				result.append("<f2>" + intervalString);
				printedAField = true;
			}

			if (printedAField) {
				result.append("|");
			}
			printedAField = true;
			result.append("<f3>" + termString);

			if (propertyName != null) {
				Property h = node.properties.get(propertyName);
				if (h != null && h.lattice.value() != null) {
					if (printedAField) {
						result.append("|");
					}
					result.append("<f4>" + escapeBrackets(escapeString(h.lattice.value().toString())));
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

	public String toGraphviz(String name) {
		return toGraphviz(name, true, true, true);
	}

	public String toGraphviz() {
		return toGraphviz(null);
	}
}
