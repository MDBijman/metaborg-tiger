package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.spoofax.terms.util.NotImplementedException;

public abstract class SingleAnalysis implements IAnalysis {
	public final String name;
	public final String propertyName;
	public final Direction direction;
	public final int analysisId;

	private static final boolean DEBUG = Flock.DEBUG;

	public SingleAnalysis(String name, Direction dir, int analysisId) {
		this.name = name;
		this.propertyName = name;
		this.direction = dir;
		this.analysisId = analysisId;
	}

	public void addToNew(SCCs sccs, Node n) {
		n.initialized.set(this.analysisId, false);
		this.addToDirty(n.component);
	}

	public void addToDirty(Component c) {
		c.clean.set(this.analysisId, false);
	}

	public void addToClean(Component c) {
		c.clean.set(this.analysisId, true);
		for (Node n : c.nodes) {
			n.initialized.set(this.analysisId, true);
		}
	}

	public void remove(Component c) {
		c.clean.set(this.analysisId, false);
	}

	public boolean isDirty(Component c) {
		return !c.clean.get(this.analysisId);
	}

	public boolean isClean(Component c) {
		return c.clean.get(this.analysisId);
	}

	public String getName() {
		return this.name;
	}

	public Direction getDirection() {
		return this.direction;
	}

	@Override
	public void removeAnalysisResults(SCCs sccs, Component c) {
		if (this.direction == Direction.FORWARD) {
			this.markCleanSuccessors(sccs, c);
		} else if (this.direction == Direction.BACKWARD) {
			this.markCleanPredecessors(sccs, c);
		}
	}

	private void markCleanPredecessors(SCCs sccs, Component c) {
		Stack<Component> todo = new Stack<Component>();
		todo.add(c);
		this.removeNodeResults(sccs, c.nodes);

		while (!todo.empty()) {
			Component next = todo.pop();
			for (Component neighbour : next.parents) {
				if (this.isClean(neighbour)) {
					this.removeNodeResults(sccs, neighbour.nodes);
					todo.add(neighbour);
				}
			}
		}
	}

	private void markCleanSuccessors(SCCs sccs, Component c) {
		Stack<Component> todo = new Stack<Component>();
		todo.add(c);
		this.removeNodeResults(sccs, c.nodes);

		while (!todo.empty()) {
			Component next = todo.pop();
			for (Component neighbour : next.children) {
				if (this.isClean(neighbour)) {
					this.removeNodeResults(sccs, neighbour.nodes);
					todo.add(neighbour);
				}
			}
		}
	}

	public void validate(Graph g, SCCs sccs) {
		if (!DEBUG)
			return;
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
		this.performDataAnalysis(g, sccs, n.component);
	}

	/**
	 * 
	 * 
	 * @param cfg    The CFG
	 * @param sccs   Strongly Connected Components of cfg
	 * @param target Component containing the node which we are querying
	 */
	public void performDataAnalysis(Graph cfg, SCCs sccs, Component target) {
		if (!this.isDirty(target)) {
			return;
		}

		if (this.direction == Direction.FORWARD) {
			for (Component pred : target.parents) {
				this.performDataAnalysis(cfg, sccs, pred);
			}
		} else {
			for (Component succ : target.children) {
				this.performDataAnalysis(cfg, sccs, succ);
			}
		}

		performDataAnalysisSingleComponent(cfg, sccs, target);
	}

	private void performDataAnalysisSingleComponent(Graph cfg, SCCs sccs, Component target) {
		Collection<Node> componentNodes = target.nodes;
		Collection<Node> newNodes = componentNodes.stream().filter(x -> !x.initialized.get(analysisId))
				.collect(Collectors.toSet());

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
		for (Node n : target.nodes) {
			if (n.type == NodeType.START) {
				n.getProperty(this.propertyName).init.eval(this.direction, n.getProperty(this.propertyName).lattice, n);
			}
		}
		Flock.endTime("Analysis@loop2");

		Flock.beginTime("Analysis@loop3");
		for (Node node : newNodes) {
			Flock.increment("Analysis@loop3");
			FlockLattice init = node.getProperty(this.propertyName).lattice;
			for (Node pred : this.getPredecessors(node)) {
				pred.getProperty(this.propertyName).transfer.eval(this.direction, init, pred);
			}
			node.getProperty(this.propertyName).lattice = init;
		}
		Flock.endTime("Analysis@loop3");

		Flock.beginTime("Analysis@worklist");
		while (!worklist.isEmpty()) {
			Node node = worklist.poll();

			Flock.increment("Analysis@worklist");

			Set<Node> successors = this.getSuccessors(node);

			for (Node successor : successors) {
				if (!target.nodes.contains(successor))
					continue;

				boolean changed = false;

				FlockLattice values_o = successor.getProperty(this.propertyName).lattice;

				changed = node.getProperty(this.propertyName).transfer.eval(this.direction, values_o, node);

				if (changed && !worklist.contains(successor)) {
					worklist.add(successor);
				}

				Flock.increment("Analysis@worklist");
			}
		}
		Flock.endTime("Analysis@worklist");
		Flock.beginTime("Analysis@markClean");
		this.addToClean(target);
		Flock.endTime("Analysis@markClean");
		Flock.endTime("Analysis@" + this.name);

	}

	private Set<Node> getPredecessors(Node n) {
		if (this.direction == Direction.BACKWARD) {
			return n.children;
		} else {
			return n.parents;
		}
	}

	private Set<Node> getSuccessors(Node n) {
		if (this.direction == Direction.FORWARD) {
			return n.children;
		} else {
			return n.parents;
		}
	}
}