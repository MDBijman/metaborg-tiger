package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.spoofax.terms.util.NotImplementedException;

public abstract class Analysis {
	public enum Direction {
		FORWARD, BACKWARD
	}

	public final String name;
	public final String propertyName;
	public final Direction direction;
	public HashSet<Node> cleanNodes = new HashSet<>();
	public HashSet<Node> dirtyNodes = new HashSet<>();
	public HashSet<Node> newNodes = new HashSet<>();
	private boolean hasRunOnce = false;
	private boolean debug = false;

	public Analysis(String name, Direction dir) {
		this.name = name;
		this.propertyName = name;
		this.direction = dir;
	}

	public void addToDirty(Node n) {
		this.dirtyNodes.add(n);
		this.cleanNodes.remove(n);
		this.newNodes.remove(n);
	}

	public void addToNew(Node n) {
		this.newNodes.add(n);
		this.cleanNodes.remove(n);
		this.dirtyNodes.remove(n);
	}

	public void addToClean(Node n) {
		this.cleanNodes.add(n);
		this.newNodes.remove(n);
		this.dirtyNodes.remove(n);
	}

	public void remove(Set<Node> nodes) {
		this.dirtyNodes.removeAll(nodes);
		this.newNodes.removeAll(nodes);
		this.cleanNodes.removeAll(nodes);
	}

	public void clear() {
		this.newNodes.clear();
		this.dirtyNodes.clear();
		this.cleanNodes.clear();
	}

	public void validate(Graph g) {
		if (!debug)
			return;

		for (Node n : this.cleanNodes) {
			if (this.dirtyNodes.contains(n)) {
				throw new RuntimeException("Clean node also in dirty list");
			}
			if (this.newNodes.contains(n)) {
				throw new RuntimeException("Clean node also in new list");
			}
		}
		for (Node n : this.dirtyNodes) {
			if (this.newNodes.contains(n)) {
				throw new RuntimeException("Dirty node also in new list");
			}
		}
		for (Node n : g.nodes()) {
			if (!this.cleanNodes.contains(n) && !this.dirtyNodes.contains(n) && !this.newNodes.contains(n)) {
				throw new RuntimeException("Node is neither dirty, clean, or new");
			}
		}
	}

	/*
	 * Analysis Logic
	 */

	public void removeNodeResults(Set<Node> nodes) {
		for (Node n : nodes) {
			this.addToNew(n);
		}
	}

	public void removeResultAfterBoundary(Graph graph, float boundary) {
		Flock.beginTime("Analysis@removeResultAfterBoundary");
		Set<Node> nodes = this.dirtyNodes.stream().filter(n -> !this.withinBoundary(n.interval, boundary))
				.collect(Collectors.toSet());
		for (Node n : nodes) {
			this.addToNew(n);
		}

		nodes = this.cleanNodes.stream().filter(n -> !this.withinBoundary(n.interval, boundary))
				.collect(Collectors.toSet());
		for (Node n : nodes) {
			this.addToNew(n);
		}
		Flock.endTime("Analysis@removeResultAfterBoundary");
	}

	public void updateResultUntilBoundary(Graph graph, Node node) {
		Flock.beginTime("Analysis@updateResultUntilBoundary");
		float boundary = graph.intervalOf(node);

		if (!this.hasRunOnce) {
			this.performDataAnalysis(graph, graph.roots(), this.newNodes, this.dirtyNodes, boundary);
			this.hasRunOnce = true;
		} else {
			this.updateDataAnalysis(graph, this.newNodes, this.dirtyNodes, boundary);
		}

		Flock.endTime("Analysis@updateResultUntilBoundary");
	}

	/**
	 * Returns all nodes in g that have an interval before each node in nodes.
	 */
	private Set<Node> getAllPredecessors(Graph g, Set<Node> nodes) {
		Set<Node> res = new HashSet<>();
		if (nodes.size() == 0) {
			return res;
		}

		float farthest = getFarthest(nodes);
		for (Node w : g.nodes()) {
			if (this.withinBoundary(w.interval, farthest)) {
				res.add(w);
			}
		}
		return res;
	}

	/**
	 * Returns all nodes in g that have an interval after each node in nodes.
	 */
	private Set<Node> getAllSuccessors(Graph g, Set<Node> nodes) {
		Set<Node> res = new HashSet<>();
		if (nodes.size() == 0) {
			return res;
		}

		float farthest = getFarthest(nodes);
		for (Node w : g.nodes()) {
			if (!this.withinBoundary(w.interval, farthest)) {
				res.add(w);
			}
		}
		return res;
	}

	/**
	 * Returns the farthest interval in the given set of nodes. If the analysis is
	 * forward, then the interval is the highest float found. If the analysis is
	 * backward, then the interval is the lowest float found.
	 */
	private float getFarthest(Set<Node> nodes) {
		float farthest = -1.0f;
		for (Node n : this.dirtyNodes) {
			if (farthest == -1.0f) {
				farthest = n.interval;
			} else if (!this.withinBoundary(n.interval, farthest)) {
				farthest = n.interval;
			}
		}

		return farthest;
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

	public abstract void initNodeTransferFunction(Node node);

	public void performDataAnalysis(Graph g) {
		performDataAnalysis(g, g.roots(), g.nodes(), new HashSet<Node>());
	}

	public void performDataAnalysis(Graph g, Collection<Node> roots, Collection<Node> nodeset, Collection<Node> dirty) {
		if (this.direction == Direction.BACKWARD) {
			performDataAnalysis(g, roots, nodeset, dirty, -Float.MAX_VALUE);
		} else if (this.direction == Direction.FORWARD) {
			performDataAnalysis(g, roots, nodeset, dirty, Float.MAX_VALUE);
		}
	}

	public void updateDataAnalysis(Graph g, Collection<Node> newNodes, Collection<Node> dirty, float intervalBoundary) {
		performDataAnalysis(g, new HashSet<Node>(), newNodes, dirty, intervalBoundary);
	}

	/**
	 * 
	 * 
	 * @param cfg              The CFG
	 * @param roots            Roots of the CFG
	 * @param nodeset          New nodes
	 * @param dirty            Dirty nodes
	 * @param intervalBoundary Boundary within which nodes must fall to be updated
	 */
	public void performDataAnalysis(Graph cfg, Collection<Node> roots, Collection<Node> nodeset, Collection<Node> dirty,
			float intervalBoundary) {
		Queue<Node> worklist = new LinkedBlockingQueue<>();

		Collection<Node> boundaryFilteredNodeset = nodeset.stream()
				.filter(n -> this.withinBoundary(n.interval, intervalBoundary)).collect(Collectors.toSet());
		
		Flock.beginTime("analysis@loop1");
		for (Node node : dirty) {
			if (!this.withinBoundary(node.interval, intervalBoundary))
				continue;

			worklist.add(node);
			initNodeTransferFunction(node);
		}
		Flock.endTime("analysis@loop1");

		/*
		 * We loop over the nodeset twice because we need to init each node before we
		 * can evaluate the transfer function for the first time, since we might visit
		 * successors of an unitialized node before visiting the node itself.
		 */
		Flock.beginTime("analysis@loop2");
		for (Node node : boundaryFilteredNodeset) {
			initNodeValue(node);
			initNodeTransferFunction(node);

			if (roots.contains(node)) {
				node.getProperty(this.propertyName).lattice = node.getProperty(this.propertyName).init.eval(node);
			}

			worklist.add(node);
		}
		Flock.endTime("analysis@loop2");

		Flock.beginTime("analysis@loop3");
		for (Node node : boundaryFilteredNodeset) {
			FlockLattice init = node.getProperty(this.propertyName).lattice;
			for (Node pred : this.getPredecessors(cfg, node)) {
				FlockLattice eval_lat = pred.getProperty(this.propertyName).transfer.eval(pred);
				init = init.lub(eval_lat);
			}
			node.getProperty(this.propertyName).lattice = init;
		}
		Flock.endTime("analysis@loop3");

		Flock.beginTime("analysis@worklist");
		while (!worklist.isEmpty()) {
			
			Node node = worklist.poll();

			Flock.increment("worklist-iteration");

			FlockLattice values_n = node.getProperty(this.propertyName).transfer.eval(node);

			Set<Node> successors = this.getSuccessors(cfg, node);

			for (Node successor : successors) {
				if (!this.withinBoundary(successor.interval, intervalBoundary)) {
					continue;
				}

				FlockLattice values_o = successor.getProperty(this.propertyName).lattice;
				
				Flock.beginTime("analysis@lub");
				
				FlockLattice lub = values_o.lub(values_n);
				boolean changed = !lub.equals(values_o);
				successor.getProperty(this.propertyName).lattice = lub;
				
				Flock.endTime("analysis@lub");
				
				if (changed) {
					worklist.add(successor);
				}
				
				Flock.increment("worklist-iteration-lub");
			}
		}
		Flock.endTime("analysis@worklist");

		// Remove the updated nodes from the dirty/new collections
		// And add them to the clean nodes
		Flock.beginTime("analysis@update-nodes");
		Set<Node> nodes = this.dirtyNodes.stream().filter(n -> this.withinBoundary(n.interval, intervalBoundary))
				.collect(Collectors.toSet());
		for (Node n : nodes) {
			this.addToClean(n);
		}

		nodes = this.newNodes.stream().filter(n -> this.withinBoundary(n.interval, intervalBoundary))
				.collect(Collectors.toSet());
		for (Node n : nodes) {
			this.addToClean(n);
		}
		Flock.endTime("analysis@update-nodes");
	}

	private Set<Node> getPredecessors(Graph g, Node n) {
		if (this.direction == Direction.BACKWARD) {
			return g.childrenOf(n);
		} else {
			return g.parentsOf(n);
		}
	}

	private Set<Node> getSuccessors(Graph g, Node n) {
		if (this.direction == Direction.FORWARD) {
			return g.childrenOf(n);
		} else {
			return g.parentsOf(n);
		}
	}
}
