package org.metaborg.lang.tiger.flock.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MaySet;
import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.SetUtils;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class LiveVariables extends Analysis {
	public LiveVariables() {
		super("live", Direction.BACKWARD);
	}

	@Override
	public void initNodeValue(Node node) {
		node.addProperty("live", MaySet.bottom());
	}

	public static void initNodeTransferFunction(Node node) {
		{
			if (true) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction0;
			}
			if (TermUtils.isAppl(node.term)
					&& (M.appl(node.term).getName().equals("Var") && node.term.getSubtermCount() == 1)) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction1;
			}
			if (TermUtils.isAppl(node.term)
					&& (M.appl(node.term).getName().equals("VarDec") && node.term.getSubtermCount() == 3)) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction2;
			}
			if (true) {
				node.getProperty("live").init = TransferFunctions.TransferFunction3;
			}
		}
	}

	@Override
	public void performDataAnalysis(Graph g, Collection<Node> roots, Collection<Node> nodeset, Collection<Node> dirty,
			float intervalBoundary) {
		Queue<Node> worklist = new LinkedBlockingQueue<>();
		HashSet<Node> inWorklist = new HashSet<>();
		for (Node node : nodeset) {
			if (node.interval < intervalBoundary)
				continue;
			worklist.add(node);
			inWorklist.add(node);
			initNodeValue(node);
			initNodeTransferFunction(node);
		}
		for (Node node : dirty) {
			if (node.interval < intervalBoundary)
				continue;
			worklist.add(node);
			inWorklist.add(node);
			initNodeTransferFunction(node);
		}
		for (Node root : roots) {
			if (root.interval < intervalBoundary)
				continue;
			root.getProperty("live").lattice = root.getProperty("live").init.eval(root);
			this.changedNodes.add(root);
		}
		for (Node node : nodeset) {
			if (node.interval < intervalBoundary)
				continue;
			{
				FlockLattice init = node.getProperty("live").lattice;
				for (Node pred : g.childrenOf(node)) {
					FlockLattice live_o = pred.getProperty("live").transfer.eval(pred);
					init = init.lub(live_o);
				}
				node.getProperty("live").lattice = init;
				this.changedNodes.add(node);
			}
		}
		while (!worklist.isEmpty()) {
			Node node = worklist.poll();
			inWorklist.remove(node);
			if (node.interval < intervalBoundary)
				continue;
			FlockLattice live_n = node.getProperty("live").transfer.eval(node);
			for (Node successor : g.childrenOf(node)) {
				if (successor.interval < intervalBoundary)
					continue;
				boolean changed = false;
				if (changed && !inWorklist.contains(successor)) {
					worklist.add(successor);
					inWorklist.add(successor);
				}
				if (changed) {
					this.changedNodes.add(successor);
				}
			}
			for (Node successor : g.parentsOf(node)) {
				boolean changed = false;
				if (successor.interval < intervalBoundary)
					continue;
				FlockLattice live_o = successor.getProperty("live").lattice;
				if (live_n.nleq(live_o)) {
					successor.getProperty("live").lattice = live_o.lub(live_n);
					changed = true;
				}
				if (changed && !inWorklist.contains(successor)) {
					worklist.add(successor);
					inWorklist.add(successor);
				}
				if (changed) {
					this.changedNodes.add(successor);
				}
			}
		}
	}
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
	public static TransferFunction TransferFunction3 = new TransferFunction3();
}

class TransferFunction0 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Node next = node;
		MaySet tmp76 = (MaySet) UserFunctions.live_f(next);
		return new MaySet(tmp76);
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set tmp79 = (Set) SetUtils.create(usrn);
		Set tmp80 = (Set) SetUtils.union(tmp79, UserFunctions.live_f(next).value());
		Set tmp75 = (Set) tmp80;
		return new MaySet(tmp75);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set result2 = new HashSet();
		for (Object usrm : (Set) UserFunctions.live_f(next).value()) {
			if (!usrm.equals(usrn)) {
				result2.add(usrm);
			}
		}
		Set tmp78 = (Set) result2;
		Set tmp74 = (Set) tmp78;
		return new MaySet(tmp74);
	}
}

class TransferFunction3 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Set tmp77 = (Set) SetUtils.create();
		Set tmp73 = (Set) tmp77;
		return new MaySet(tmp73);
	}
}

class UserFunctions {
	public static FlockLattice live_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("live").lattice;
	}
}