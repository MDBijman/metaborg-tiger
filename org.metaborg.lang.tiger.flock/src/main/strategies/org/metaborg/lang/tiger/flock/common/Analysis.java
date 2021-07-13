package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockCollectionLattice;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
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
	public HashSet<Node> changedNodes = new HashSet<>();
	private boolean hasRunOnce = false;

	public Analysis(String name, Direction dir) {
		this.name = name;
		this.propertyName = name;
		this.direction = dir;
	}

	public Analysis(String name, String propertyName, Direction dir) {
		this.name = name;
		this.propertyName = propertyName;
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
		
		for (Node node : nodes) {
			this.dependents.remove(node.getId());
		}

		for (Set<Dependency> s : this.dependents.values()) {
			for (Node node : nodes) {
				s.remove(new Dependency(node.getId()));
			}
		}
		
		for (Node n : g.nodes()) {
			if (n.isGhost) {
				continue;
			}
			
			Property prop = n.getProperty(this.propertyName);
			
			if (prop == null) {
				continue;
			}
			
		}
	}

	public void clear() {
		this.newNodes.clear();
		this.dirtyNodes.clear();
		this.dependents.clear();
	}

	/*
	 * Analysis Logic
	 */

	public void updateUntilBoundary(Graph graph, Node node) {
		float boundary = graph.intervalOf(node);
		Set<Node> dirtyNodes = new HashSet<>(this.dirtyNodes);

		for (Node n : this.dirtyNodes) {
			dirtyNodes.addAll(getTermDependencies(graph, n));
		}
		for (Node n : this.newNodes) {
			dirtyNodes.addAll(getTermDependencies(graph, n));
		}

		if (!this.hasRunOnce) {
			this.performDataAnalysis(graph, graph.roots(), this.newNodes, dirtyNodes, boundary);
			this.hasRunOnce = true;
		} else {
			this.updateDataAnalysis(graph, this.newNodes, dirtyNodes, boundary);
		}

		removeNodesUntilBoundary(graph, boundary);
	}

	private void removeNodesUntilBoundary(Graph graph, float boundary) {
		if (this.direction == Direction.FORWARD) {
			this.dirtyNodes.removeIf(n -> graph.intervalOf(n) <= boundary);
			this.newNodes.removeIf(n -> graph.intervalOf(n) <= boundary);
		} else if (this.direction == Direction.BACKWARD) {
			this.dirtyNodes.removeIf(n -> graph.intervalOf(n) >= boundary);
			this.newNodes.removeIf(n -> graph.intervalOf(n) >= boundary);
		}
	}

	/*
	 * Analysis specific methods
	 */
	
	// FIXME autogenerate
	// Add this as abstract method to generated analysis file and invoke
	public abstract Set<Node> getTermDependencies(Graph g, Node n);

	// Maps node to all the nodes that depend on it
	private HashMap<CfgNodeId, Set<Dependency>> dependents = new HashMap<>();
	
	public void updateDependents(Set<Node> nodes) {
		Flock.beginTime("Analysis@updateDependents");
		for (Node n : nodes) {
			if (n.isGhost) {
				continue;
			}
			
			Property prop = n.getProperty(this.propertyName);
			
			if (prop == null) {
				continue;
			}
			
			FlockLattice l = prop.lattice;
			
			if (l == null) {
				throw new RuntimeException("Lattice doesn't have dependency tracking: " + this.name);
			}
			for (Dependency d : LatticeDependencyUtils.gatherDependencies(l)) {
				dependents.putIfAbsent(d.id, new HashSet<>());
				dependents.get(d.id).add(new Dependency(n.getId()));
			}
		}
		Flock.endTime("Analysis@updateDependents");
	}
	
	public void removeFacts(Graph g, CfgNodeId origin) {
		Flock.beginTime("Analysis@removeFacts");
		
		updateDependents(this.changedNodes);
		this.changedNodes.clear();
		
		Set<Dependency> deps = dependents.get(origin);
		if (deps == null) return;

		
		for (Dependency d : deps) {
			Node n = g.getNode(d.id);
			
			if (n.isGhost) {
				continue;
			}
			
			Property prop = n.getProperty(this.propertyName);
			
			if (prop == null) {
				continue;
			}
			
			FlockCollectionLattice l = (FlockCollectionLattice) prop.lattice;
			
			if (l == null) {
				throw new RuntimeException("Lattice doesn't have dependency tracking: " + this.name);
			}
			
			LatticeDependencyUtils.removeValuesByDependency(l, new Dependency(origin));
			this.addToDirty(n);
		}
		
		Flock.endTime("Analysis@removeFacts");
	}
	

	/*
	 * Analysis implementation
	 */
	
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
