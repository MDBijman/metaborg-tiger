package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.spoofax.terms.util.NotImplementedException;

public abstract class Analysis {
	public enum Direction {
		FORWARD, BACKWARD
	}

	public final String name;
	public final String propertyName;
	public final Direction direction;
//	public HashSet<Node> cleanNodes = new HashSet<>();
//	public HashSet<Node> dirtyNodes = new HashSet<>();
//	public HashSet<Node> newNodes = new HashSet<>();

	public HashSet<Component> cleanComponents = new HashSet<>();
	public HashSet<Component> dirtyComponents = new HashSet<>();
	public HashSet<Node> newNodes = new HashSet<>();

	private boolean hasRunOnce = false;
	private boolean debug = false;

	public Analysis(String name, Direction dir) {
		this.name = name;
		this.propertyName = name;
		this.direction = dir;
	}

	public void addToNew(SCCs sccs, Node n) {
		this.newNodes.add(n);
		this.addToDirty(sccs.nodeComponent.get(n));
	}

	public void addToDirty(Component c) {
		this.cleanComponents.remove(c);
		this.dirtyComponents.add(c);
	}

	public void addToClean(Component c) {
		this.cleanComponents.add(c);
		this.dirtyComponents.remove(c);
		for (Node n : c.nodes) {
			this.newNodes.remove(n);
		}
	}

	public void remove(Component c) {
		this.cleanComponents.remove(c);
		this.dirtyComponents.remove(c);
		for (Node n : c.nodes) {
			this.newNodes.remove(n);
		}
	}

	public void remove(Node n) {
		this.newNodes.remove(n);
	}

	public void clear() {
		this.newNodes.clear();
		this.cleanComponents.clear();
		this.dirtyComponents.clear();
	}

	public void validate(Graph g, SCCs sccs) {
		if (!debug)
			return;

		for (Component c : this.cleanComponents) {
			if (this.dirtyComponents.contains(c)) {
				throw new RuntimeException("Clean component also in dirty list");
			}
			for (Node n : c.nodes) {
				if (this.newNodes.contains(n)) {
					throw new RuntimeException("New node in clean component");
				}
			}
		}

		for (Component c : sccs.components) {
			if (!this.cleanComponents.contains(c) && !this.dirtyComponents.contains(c)) {
				throw new RuntimeException("Component is neither dirty nor clean");
			}
		}
	}

	/*
	 * Analysis Logic
	 */

	public void removeNodeResults(SCCs sccs, Set<Node> nodes) {
		for (Node n : nodes) {
			this.addToNew(sccs, n);
		}
	}

	/*
	 * Analysis implementation
	 */

	public abstract void initNodeValue(Node node);

	public abstract void initNodeTransferFunction(Node node);

	public void performDataAnalysis(Graph g, SCCs sccs, Node n) {
		this.performDataAnalysis(g, sccs, sccs.nodeComponent.get(n));
	}

	/**
	 * 
	 * 
	 * @param cfg    The CFG
	 * @param sccs   Strongly Connected Components of cfg
	 * @param target Component containing the node which we are querying
	 */
	public void performDataAnalysis(Graph cfg, SCCs sccs, Component target) {
		if (this.direction == Direction.FORWARD) {
			for (Component pred : sccs.revNeighbours.get(target)) {
				if (this.dirtyComponents.contains(pred)) {
					this.performDataAnalysis(cfg, sccs, pred);
				}
			}
		} else {
			for (Component succ : sccs.neighbours.get(target)) {
				if (this.dirtyComponents.contains(succ)) {
					this.performDataAnalysis(cfg, sccs, succ);
				}
			}
		}

		performDataAnalysisSingleComponent(cfg, sccs, target);
	}

	private void performDataAnalysisSingleComponent(Graph cfg, SCCs sccs, Component target) {
		Collection<Node> componentNodes = target.nodes;
		Collection<Node> newNodes = componentNodes.stream().filter(this.newNodes::contains).collect(Collectors.toSet());

		Queue<Node> worklist = new LinkedBlockingQueue<>(componentNodes);

		Flock.beginTime("analysis@loop1");
		for (Node node : componentNodes) {
			initNodeValue(node);
			initNodeTransferFunction(node);
		}
		Flock.endTime("analysis@loop1");

		/*
		 * We loop over the nodeset twice because we need to init each node before we
		 * can evaluate the transfer function for the first time, since we might visit
		 * successors of an uninitialized node before visiting the node itself.
		 */
		Flock.beginTime("analysis@loop2");
		for (Node node : newNodes) {
			if (cfg.roots().contains(node)) {
				node.getProperty(this.propertyName).lattice = node.getProperty(this.propertyName).init.eval(node);
			}

			worklist.add(node);
		}
		Flock.endTime("analysis@loop2");

		Flock.beginTime("analysis@loop3");
		for (Node node : newNodes) {
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
				if (!target.nodes.contains(successor))
					continue;

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

		this.addToClean(target);
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
