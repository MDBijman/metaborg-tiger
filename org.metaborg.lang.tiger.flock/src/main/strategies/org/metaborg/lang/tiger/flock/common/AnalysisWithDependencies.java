package org.metaborg.lang.tiger.flock.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockCollectionLattice;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;

public abstract class AnalysisWithDependencies extends Analysis {

	// Maps node to all the nodes that depend on it
	private HashMap<TermId, Set<Dependency>> dependents = new HashMap<>();
	public HashSet<Node> changedNodes = new HashSet<>();

	public AnalysisWithDependencies(String name, Direction dir) {
		super(name, dir);
	}
	
	@Override
	public void clear() {
		this.dependents.clear();
		super.clear();
	}
	
	@Override
	public Set<Node> getNodesBefore(Graph g, Node n)
	{
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void remove(Graph g, Set<Node> nodes) {
		for (Node node : nodes) {
			this.dependents.remove(node.getId());
		}

		for (Set<Dependency> s : this.dependents.values()) {
			for (Node node : nodes) {
				s.remove(new Dependency(node.getId()));
			}
		}

		super.remove(g, nodes);
	}
	
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

	public void removeFacts(Graph g, TermId origin) {
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
}
