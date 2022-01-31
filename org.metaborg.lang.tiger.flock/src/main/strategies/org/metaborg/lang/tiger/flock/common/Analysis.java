package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayDeque;
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
		if (!this.dirtyComponents.contains(target)) {
			return;
		}

		if (this.direction == Direction.FORWARD) {
			for (Component pred : sccs.revNeighbours.get(target)) {
				this.performDataAnalysis(cfg, sccs, pred);
			}
		} else {
			for (Component succ : sccs.neighbours.get(target)) {
				this.performDataAnalysis(cfg, sccs, succ);
			}
		}

		performDataAnalysisSingleComponent(cfg, sccs, target);
	}

	private void performDataAnalysisSingleComponent(Graph cfg, SCCs sccs, Component target) {
		Collection<Node> componentNodes = target.nodes;
		Collection<Node> newNodes = componentNodes.stream().filter(this.newNodes::contains).collect(Collectors.toSet());

		Queue<Node> worklist = new ArrayDeque<>(componentNodes);

		Flock.beginTime("Analysis@" + this.name);
		Flock.beginTime("Analysis@loop1");
		for (Node node : componentNodes) {
			initNodeValue(node);
			initNodeTransferFunction(node);
		}
		Flock.endTime("Analysis@loop1");

		/*
		 * We loop over the nodeset twice because we need to init each node before we
		 * can evaluate the transfer function for the first time, since we might visit
		 * successors of an uninitialized node before visiting the node itself.
		 */
		Flock.beginTime("Analysis@loop2");
		for (Node node : newNodes) {
			if (cfg.roots().contains(node)) {
				node.getProperty(this.propertyName).lattice = node.getProperty(this.propertyName).init.eval(node);
			}
		}
		Flock.endTime("Analysis@loop2");
		
		Flock.beginTime("Analysis@loop3");
		for (Node node : newNodes) {
			Flock.increment("Analysis@loop3");
			FlockLattice init = node.getProperty(this.propertyName).lattice;
			for (Node pred : this.getPredecessors(cfg, node)) {
				if (pred.getProperty(this.propertyName).transfer.supportsInplace()) {
					Flock.beginTime("a1");
					pred.getProperty(this.propertyName).transfer.evalInplace(init, pred);
					Flock.endTime("a1");
				} else {
					FlockLattice tmp = pred.getProperty(this.propertyName).transfer.eval(pred);

					if (this.getPredecessors(cfg, node).size() == 1) {
						init = tmp;
					} else {
						init.lubInplace(tmp);
					}
				}
			}
			node.getProperty(this.propertyName).lattice = init;
		}
		Flock.endTime("Analysis@loop3");

		Flock.beginTime("Analysis@worklist");
		while (!worklist.isEmpty()) {
			Node node = worklist.poll();

			Flock.increment("Analysis@worklist");

			Set<Node> successors = this.getSuccessors(cfg, node);

			for (Node successor : successors) {
				if (!target.nodes.contains(successor))
					continue;

				boolean changed = false;

				FlockLattice values_o = successor.getProperty(this.propertyName).lattice;

				if (node.getProperty(this.propertyName).transfer.supportsInplace()) {
					changed = node.getProperty(this.propertyName).transfer.evalInplace(values_o, node);
				} else {
					FlockLattice eval = node.getProperty(this.propertyName).transfer.eval(node);
					changed = values_o.lubInplace(eval);
				}

				if (changed) {
					worklist.add(successor);
				}

				Flock.increment("Analysis@worklist");
			}
		}
		Flock.endTime("Analysis@worklist");
		Flock.endTime("Analysis@" + this.name);
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
