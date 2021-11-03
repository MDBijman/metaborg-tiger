package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockCollectionLattice;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.value.FlowAnalysis;
import org.spoofax.terms.util.NotImplementedException;
import org.spoofax.terms.util.TermUtils;

public abstract class Analysis {
	public enum Direction {
		FORWARD, BACKWARD
	}

	public final String name;
	public final String propertyName;
	public final Direction direction;
	public HashSet<Node> dirtyNodes = new HashSet<>();
	public HashSet<Node> newNodes = new HashSet<>();
	private boolean hasRunOnce = false;

	public Analysis(String name, Direction dir) {
		this.name = name;
		this.propertyName = name;
		this.direction = dir;
	}

	public void addToDirty(Node n) {
		this.dirtyNodes.add(n);
	}

	public void addToNew(Node n) {
		this.newNodes.add(n);
	}

	public void remove(Graph g, Set<Node> nodes) {
		this.dirtyNodes.removeAll(nodes);
		this.newNodes.removeAll(nodes);
	}

	public void clear() {
		this.newNodes.clear();
		this.dirtyNodes.clear();
	}

	/*
	 * Analysis Logic
	 */

	public void removeNodeResults(Set<Node> nodes) {
		for (Node n : nodes) {
			this.addToDirty(n);
			this.initNodeValue(n);
		}
	}

	public void removeResultAfterBoundary(Graph graph, float boundary) {
		for (Node n : graph.nodes()) {
			if (!this.withinBoundary(boundary, n.interval)) {
				continue;
			}

			this.addToDirty(n);
			this.initNodeValue(n);
		}
	}

	public void updateResultUntilBoundary(Graph graph, Node node) {
		Flock.beginTime("Analysis@updateResultUntilBoundary");
		float boundary = graph.intervalOf(node);
		Set<Node> dirtyNodes = new HashSet<>(this.dirtyNodes);

		Flock.beginTime("Analysis@getDirtyNodesDependencies");
		dirtyNodes.addAll(getNodesBefore(graph, this.dirtyNodes));
		Flock.endTime("Analysis@getDirtyNodesDependencies");
		Flock.beginTime("Analysis@getNewNodesDependencies");
		dirtyNodes.addAll(getNodesBefore(graph, this.newNodes));
		Flock.endTime("Analysis@getNewNodesDependencies");

		if (!this.hasRunOnce) {
			this.performDataAnalysis(graph, graph.roots(), this.newNodes, dirtyNodes, boundary);
			this.hasRunOnce = true;
			
			// Remove the updated nodes from the dirty/new collections
			this.dirtyNodes.removeIf(n -> this.withinBoundary(graph.intervalOf(n), boundary));
			this.newNodes.removeIf(n   -> this.withinBoundary(graph.intervalOf(n), boundary));
		} else {
			this.updateDataAnalysis(graph, this.newNodes, dirtyNodes, boundary);
		}
		
		Flock.endTime("Analysis@updateResultUntilBoundary");
	}

	private Set<Node> getNodesBefore(Graph g, Set<Node> nodes) {
		Set<Node> res = new HashSet<>();
		if (nodes.size() == 0) {
			return res;
		}
		
		float furthest = -1.0f; 
		for (Node n : this.dirtyNodes) {
			//dirtyNodes.addAll(getTermDependencies(graph, n));
			if (furthest == -1.0f) {
				furthest = n.interval;
			} else if (!this.withinBoundary(n.interval, furthest)) {
				furthest = n.interval;
			}
		}
		
		for (Node w : g.nodes()) {
			if (this.withinBoundary(furthest, w.interval)) {
				res.add(w);
			}
		}
		return res;
	}

	public Set<Node> getNodesBefore(Graph g, Node n) {
		// Return set of nodes after n in g
		return g.nodes().stream().filter(o -> this.withinBoundary(n.interval, o.interval)).collect(Collectors.toSet());
	}
	
	protected boolean withinBoundary(float interval, float boundary) {
		if (this.direction == Direction.FORWARD) {
			return interval <= boundary;
		} else if (this.direction == Direction.BACKWARD) {
			return interval >= boundary;
		} else {
			throw new NotImplementedException();
		}
	}

	/*
	 * Analysis implementation
	 */

	public abstract void initNodeValue(Node node);

	public void performDataAnalysis(Graph g, Node root) {
		HashSet<Node> nodeset = new HashSet<Node>();
		nodeset.add(root);
		performDataAnalysis(g, new HashSet<Node>(), nodeset);
	}

	public void performDataAnalysis(Graph g, Collection<Node> nodeset) {
		performDataAnalysis(g, new HashSet<Node>(), nodeset);
	}

	public void performDataAnalysis(Graph g) {
		performDataAnalysis(g, g.roots(), g.nodes());
	}

	public void performDataAnalysis(Graph g, Collection<Node> roots, Collection<Node> nodeset) {
		performDataAnalysis(g, roots, nodeset, new HashSet<Node>());
	}

	public void updateDataAnalysis(Graph g, Collection<Node> news, Collection<Node> dirty) {
		performDataAnalysis(g, new HashSet<Node>(), news, dirty);
	}

	public void performDataAnalysis(Graph g, Collection<Node> roots, Collection<Node> nodeset, Collection<Node> dirty) {
		if (this.direction == Direction.BACKWARD) {
			performDataAnalysis(g, roots, nodeset, dirty, -Float.MAX_VALUE);
		} else if (this.direction == Direction.FORWARD) {
			performDataAnalysis(g, roots, nodeset, dirty, Float.MAX_VALUE);
		}
	}

	public void updateDataAnalysis(Graph g, Collection<Node> news, Collection<Node> dirty, float intervalBoundary) {
		performDataAnalysis(g, new HashSet<Node>(), news, dirty, intervalBoundary);
	}

	public abstract void performDataAnalysis(Graph g, Collection<Node> roots, Collection<Node> nodeset,
			Collection<Node> dirty, float intervalBoundary);


}
