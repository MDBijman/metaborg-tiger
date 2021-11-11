package org.metaborg.lang.tiger.flock.ae;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MustSet;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.SetUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TransferFunction;

public class AvailableExpressions extends Analysis {
	public AvailableExpressions() {
		super("expressions", Direction.FORWARD);
	}

	public void initNodeValue(Node node) {
		node.addProperty("expressions", SimpleMap.bottom());
	}

	private boolean matchPattern0(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}

	private boolean matchPattern1(Node node) {
		ITerm term = node.virtualTerm;
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("VarDec") && term.childrenCount() == 3)) {
			return false;
		}
		return true;
	}

	private boolean matchPattern2(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}

	public void initNodeTransferFunction(Node node) {
		{
			if (matchPattern0(node)) {
				node.getProperty("expressions").transfer = TransferFunctions.TransferFunction0;
			}
			if (matchPattern1(node)) {
				node.getProperty("expressions").transfer = TransferFunctions.TransferFunction1;
			}
			if (matchPattern2(node)) {
				node.getProperty("expressions").init = TransferFunctions.TransferFunction2;
			}
		}
	}

	@Override
	public void performDataAnalysis(Graph g, Collection<Node> roots, Collection<Node> nodeset, Collection<Node> dirty,
			float intervalBoundary) {
		Queue<Node> worklist = new LinkedBlockingQueue<>();
		HashSet<Node> inWorklist = new HashSet<>();
		for (Node node : nodeset) {
			if (node.interval > intervalBoundary)
				continue;
			worklist.add(node);
			inWorklist.add(node);
			initNodeValue(node);
			initNodeTransferFunction(node);
		}
		for (Node node : dirty) {
			if (node.interval > intervalBoundary)
				continue;
			worklist.add(node);
			inWorklist.add(node);
			initNodeTransferFunction(node);
		}
		for (Node root : roots) {
			if (root.interval > intervalBoundary)
				continue;
			root.getProperty("expressions").lattice = root.getProperty("expressions").init.eval(root);
		}
		for (Node node : nodeset) {
			if (node.interval > intervalBoundary)
				continue;
			{
				FlockLattice init = node.getProperty("expressions").lattice;
				for (Node pred : g.parentsOf(node)) {
					FlockLattice live_o = pred.getProperty("expressions").transfer.eval(pred);
					init = init.lub(live_o);
				}
				node.getProperty("expressions").lattice = init;
			}
		}
		while (!worklist.isEmpty()) {
			Node node = worklist.poll();
			inWorklist.remove(node);
			if (node.interval > intervalBoundary)
				continue;
			FlockLattice expressions_n = node.getProperty("expressions").transfer.eval(node);
			for (Node successor : g.childrenOf(node)) {
				if (successor.interval > intervalBoundary)
					continue;
				boolean changed = false;
				FlockLattice expressions_o = successor.getProperty("expressions").lattice;
				if (expressions_n.nleq(expressions_o)) {
					successor.getProperty("expressions").lattice = expressions_o.lub(expressions_n);
					changed = true;
				}
				if (changed && !inWorklist.contains(successor)) {
					worklist.add(successor);
					inWorklist.add(successor);
				}
			}
			for (Node successor : g.parentsOf(node)) {
				boolean changed = false;
				if (successor.interval > intervalBoundary)
					continue;
				if (changed && !inWorklist.contains(successor)) {
					worklist.add(successor);
					inWorklist.add(successor);
				}
			}
		}
	}
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
}

class TransferFunction0 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Node prev = node;
		SimpleMap tmp68 = (SimpleMap) UserFunctions.expressions_f(prev);
		return tmp68;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Node prev = node;
		ITerm usrn = term.childAt(0);
		ITerm usre = term.childAt(2);
		Set tmp69 = (Set) SetUtils.create(usrn);
		Map result10 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.expressions_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			result10.put(usrk, new MustSet(SetUtils.difference(((FlockLattice) usrv).value(), tmp69)));
		}
		Map tmp70 = (Map) result10;
		Set tmp71 = (Set) SetUtils.create(usrn.toTerm());
		Map tmp72 = (Map) MapUtils.create(Helpers.toTerm(usre.toTerm()), new MustSet(tmp71));
		Map tmp81 = (Map) MapUtils.union(tmp70, tmp72);
		Map tmp67 = (Map) tmp81;
		return new SimpleMap(tmp67);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Map tmp66 = (Map) MapUtils.create();
		return new SimpleMap(tmp66);
	}
}

class UserFunctions {
	public static FlockLattice expressions_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("expressions").lattice;
	}
}