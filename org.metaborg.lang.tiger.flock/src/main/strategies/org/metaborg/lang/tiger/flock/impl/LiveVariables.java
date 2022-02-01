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
				node.getProperty("live").transfer = TransferFunctions.TransferFunction3;
			}
			if (matchPattern4(node)) {
				node.getProperty("live").transfer = TransferFunctions.TransferFunction4;
			}
			if (matchPattern5(node)) {
				node.getProperty("live").init = TransferFunctions.TransferFunction5;
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
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("LoopBinding") && term.childrenCount() == 3)) {
			return false;
		}
		ITerm term_0 = term.childAt(0);
		ITerm term_1 = term.childAt(1);
		ITerm term_2 = term.childAt(2);
		if (!(term_0.isAppl() && ((ApplTerm) term_0).getConstructor().equals("Var") && term_0.childrenCount() == 1)) {
			return false;
		}
		ITerm term_0_0 = term_0.childAt(0);
		return true;
	}

	private boolean matchPattern3(Node node) {
		ITerm term = node.virtualTerm;
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("Assign") && term.childrenCount() == 2)) {
			return false;
		}
		ITerm term_0 = term.childAt(0);
		ITerm term_1 = term.childAt(1);
		if (!(term_0.isAppl() && ((ApplTerm) term_0).getConstructor().equals("LValue")
				&& term_0.childrenCount() == 1)) {
			return false;
		}
		ITerm term_0_0 = term_0.childAt(0);
		if (!(term_0_0.isAppl() && ((ApplTerm) term_0_0).getConstructor().equals("Var")
				&& term_0_0.childrenCount() == 1)) {
			return false;
		}
		ITerm term_0_0_0 = term_0_0.childAt(0);
		return true;
	}

	private boolean matchPattern4(Node node) {
		ITerm term = node.virtualTerm;
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("VarDec") && term.childrenCount() == 3)) {
			return false;
		}
		ITerm term_0 = term.childAt(0);
		ITerm term_1 = term.childAt(1);
		ITerm term_2 = term.childAt(2);
		return true;
	}

	private boolean matchPattern5(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
	public static TransferFunction TransferFunction3 = new TransferFunction3();
	public static TransferFunction TransferFunction4 = new TransferFunction4();
	public static TransferFunction TransferFunction5 = new TransferFunction5();
}

class TransferFunction0 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		MaySet tmp100 = (MaySet) UserFunctions.live_f(next);
		return res.lub(tmp100);
	}
}

class TransferFunction1 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set tmp104 = (Set) SetUtils.create(TermUtils.asString(usrn).get());
		Set tmp105 = (Set) SetUtils.union(tmp104, ((FlockLattice) UserFunctions.live_f(next)).value());
		Set tmp99 = (Set) tmp105;
		return res.lub(new MaySet(tmp99));
	}
}

class TransferFunction2 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(term, 0), 0);
		Set result149 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result149.add(usrm);
			}
		}
		Set tmp103 = (Set) result149;
		Set tmp98 = (Set) tmp103;
		return res.lub(new MaySet(tmp98));
	}
}

class TransferFunction3 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
		Set result150 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result150.add(usrm);
			}
		}
		Set tmp102 = (Set) result150;
		Set tmp97 = (Set) tmp102;
		return res.lub(new MaySet(tmp97));
	}
}

class TransferFunction4 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set result151 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result151.add(usrm);
			}
		}
		Set tmp101 = (Set) result151;
		Set tmp96 = (Set) tmp101;
		return res.lub(new MaySet(tmp96));
	}
}

class TransferFunction5 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Set tmp95 = (Set) SetUtils.create();
		return res.lub(new MaySet(tmp95));
	}
}

class UserFunctions {
	public static FlockLattice live_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("live").lattice;
	}
}