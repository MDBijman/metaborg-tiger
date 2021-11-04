package org.metaborg.lang.tiger.flock.impl;

import java.util.HashSet;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MaySet;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.SetUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class LiveVariables extends Analysis {
	public LiveVariables() {
		super("live", Direction.BACKWARD);
	}

	@Override
	public void initNodeValue(Node node) {
		node.addProperty("live", MaySet.bottom());
	}

	@Override
	public void initNodeTransferFunction(Node node) {
		{
			if (matchPattern0(node)) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction0;
			}
			if (matchPattern1(node)) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction1;
			}
			if (matchPattern2(node)) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction2;
			}
			if (matchPattern3(node)) {
				node.getProperty("live").init = TransferFunctions.TransferFunction3;
			}
		}
	}

	private boolean matchPattern0(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}

	private boolean matchPattern1(Node node) {
		ITerm term = node.virtualTerm;
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("Var") && term.childrenCount() == 1)) {
			return false;
		}
		ITerm term_0 = term.childAt(0);
		return true;
	}

	private boolean matchPattern2(Node node) {
		ITerm term = node.virtualTerm;
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("VarDec") && term.childrenCount() == 3)) {
			return false;
		}
		ITerm term_0 = term.childAt(0);
		ITerm term_1 = term.childAt(1);
		ITerm term_2 = term.childAt(2);
		return true;
	}

	private boolean matchPattern3(Node node) {
		ITerm term = node.virtualTerm;
		return true;
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
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		MaySet tmp83 = (MaySet) UserFunctions.live_f(next);
		return tmp83;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set tmp85 = (Set) SetUtils.create(TermUtils.asString(usrn).get());
		Set tmp86 = (Set) SetUtils.union(tmp85, ((FlockLattice) UserFunctions.live_f(next)).value());
		Set tmp82 = (Set) tmp86;
		return new MaySet(tmp82);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set result122 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result122.add(usrm);
			}
		}
		Set tmp84 = (Set) result122;
		Set tmp81 = (Set) tmp84;
		return new MaySet(tmp81);
	}
}

class TransferFunction3 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Set tmp78 = (Set) SetUtils.create();
		return new MaySet(tmp78);
	}
}

class UserFunctions {
	public static FlockLattice live_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("live").lattice;
	}
}