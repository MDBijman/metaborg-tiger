package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.SCCs.Component;
import org.metaborg.lang.tiger.flock.impl.LiveVariables;
import org.metaborg.lang.tiger.flock.value.FlowAnalysis;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;

public abstract class Flock {
	public static Flock instance = new FlockIncremental();

	public IOAgent io;
	public Graph graph;
	public SCCs graph_scss;
	public TermTree termTree;
	public List<Analysis> analyses;

	public Flock() {
		this.analyses = new ArrayList<Analysis>();
		this.analyses.add(new LiveVariables());
		this.analyses.add(new FlowAnalysis());
		// this.analyses.add(new AvailableExpressions());
	}

	protected void applyGhostMask(Set<Node> mask) {
		for (Node n : this.graph.nodes()) {
			if (mask.contains(n)) {
				n.isGhost = true;
			}
		}
	}

	protected void initPosition(Graph g, ITermFactory factory) {
		int i = 0;
		for (Node n : g.nodes()) {
			n.addProperty("position", new PositionLattice(factory.makeInt(i)));
			i += 1;
		}
	}

	protected Set<Node> getAllNodes(IStrategoTerm program) {
		Set<Node> set = new HashSet<>();
		getAllNodes(set, program);
		return set;
	}

	protected void getAllNodes(Set<Node> visited, IStrategoTerm program) {
		for (IStrategoTerm term : program.getSubterms()) {
			getAllNodes(visited, term);
		}
		Node id = Helpers.getTermNode(program);
		if (id != null && this.graph.getNode(id.getId()) != null) {
			visited.add(this.graph.getNode(id.getId()));
		}
	}

	public abstract void init(IStrategoTerm program);

	public abstract void createTermGraph(IStrategoTerm term);

	public abstract void createControlFlowGraph(Context context, IStrategoTerm current);

	public abstract void removeNode(IStrategoTerm node);

	public abstract void replaceNode(IStrategoTerm node, IStrategoTerm replacement);

	public abstract Node getNode(TermId id);

	public abstract Analysis analysisWithName(String name);

	/*
	 * Helpers for mutating graph analyses
	 */

	protected void addToNew(Node n) {
		for (Analysis ga : analyses) {
			ga.addToNew(this.graph_scss, n);
		}
	}
	
	protected void addToDirty(Component c) {
		for (Analysis ga : analyses) {
			ga.addToDirty(c);
		}
	}

	protected void clearAnalyses() {
		for (Analysis ga : analyses) {
			ga.clear();
		}
	}

	/*
	 * Timing and Debugging
	 */

	public static void printDebug(String t) {
		// When called from stratego we need to print like this
		instance.io.printError(t);
		// When called from other projects stdout will work
		System.out.println(t);
	}

	private static String[] enabled = { "time", "count",
			// "debug",
			// "incremental",
			// "validation",
			"api",
			// "dependencies",
			// "graphviz"
	};
	private static HashSet<String> enabledTags = new HashSet<>(Arrays.asList(enabled));

	public static void disableLogs() {
		Flock.enabledTags = new HashSet<>();
	}

	public static void enableLogs() {
		Flock.enabledTags = new HashSet<>(Arrays.asList(enabled));
	}

	public static boolean isLogEnabled(String tag) {
		return enabledTags.contains(tag);
	}

	public static void log(String tag, String message) {
		if (enabledTags.contains(tag)) {
			printDebug("[" + tag + "]" + " " + message);
		}
	}

	private static HashMap<String, Long> runningMap = new HashMap<>();

	private static HashMap<String, Long> cumulMap = new HashMap<>();

	public static void beginTime(String tag) {
		runningMap.put(tag, System.currentTimeMillis());
		cumulMap.putIfAbsent(tag, 0L);
	}

	public static void resetTimers() {
		runningMap.clear();
		cumulMap.clear();
	}

	public static long endTime(String tag) {
		long t = System.currentTimeMillis() - runningMap.get(tag);
		runningMap.remove(tag);
		cumulMap.put(tag, cumulMap.get(tag) + t);
		return t;
	}

	public static void logTime(String tag) {
		Flock.log("time", "time " + tag + ": " + cumulMap.get(tag));
	}

	public static void logTimers() {
		ArrayList<Entry<String, Long>> entries = new ArrayList<>(cumulMap.entrySet());
		entries.sort((a, b) -> a.getKey().compareTo(b.getKey()));
		for (Entry<String, Long> e : entries) {
			Flock.log("time", e.getKey() + ": " + e.getValue());
		}
	}

	private static HashMap<String, Long> countMap = new HashMap<>();

	public static void increment(String tag) {
		countMap.putIfAbsent(tag, 0L);
		countMap.put(tag, countMap.get(tag) + 1);
	}

	public static void logCounts() {
		for (Entry<String, Long> e : countMap.entrySet()) {
			Flock.log("count", e.getKey() + ": " + e.getValue());
		}
	}

	static private TermId nextId = new TermId(0);

	public static TermId nextNodeId() {
		TermId next = Flock.nextId;
		Flock.nextId = new TermId(next.getId() + 1);
		return next;
	}
	
	public static void resetNodeId() {
		Flock.nextId = new TermId(0);
	}

}

class PositionLattice implements FlockLattice {
	IStrategoInt value;

	PositionLattice(IStrategoInt v) {
		this.value = v;
	}

	@Override
	public FlockLattice lub(FlockLattice o) {
		return null;
	}

	@Override
	public Object value() {
		return this.value;
	}

	@Override
	public IStrategoTerm toTerm() {
		return value;
	}
}