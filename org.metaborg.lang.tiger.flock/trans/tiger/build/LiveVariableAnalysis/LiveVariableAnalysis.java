package org.spoofax;

import org.apache.commons.lang3.tuple.Pair;
import org.strategoxt.lang.Context;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.terms.io.TAFTermReader;
import org.spoofax.terms.TermFactory;
import java.io.IOException;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.function.Supplier;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoList;
import flock.subject.common.Graph;
import flock.subject.common.GraphBuilder;
import flock.subject.common.TermTree;
import flock.subject.common.Analysis;
import flock.subject.common.Graph.Node;
import flock.subject.common.Analysis.Direction;
import flock.subject.common.CfgNodeId;
import flock.subject.common.Dependency;
import flock.subject.common.Helpers;
import flock.subject.common.FlockLattice;
import flock.subject.common.FlockLattice.MaySet;
import flock.subject.common.FlockLattice.MustSet;
import flock.subject.common.FlockLattice.SimpleMap;
import flock.subject.common.FlockLattice.FlockValueLattice;
import flock.subject.common.FlockLattice.FlockCollectionLattice;
import flock.subject.common.FlockValue;
import flock.subject.common.FlockValue.FlockValueWithDependencies;
import flock.subject.common.MapUtils;
import flock.subject.common.SetUtils;
import flock.subject.common.TransferFunction;
import flock.subject.common.UniversalSet;

public class FlowAnalysis extends Analysis {
	public FlowAnalysis() {
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
	public boolean eval(Analysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		MaySet tmp90 = (MaySet) UserFunctions.live_f(next);
		return TransferFunction.assignEvalResult(direction, node, res, tmp90);
	}
}

class TransferFunction1 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(Analysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set tmp94 = (Set) SetUtils.create(TermUtils.asString(usrn).get());
		Set tmp95 = (Set) SetUtils.union(tmp94, ((FlockLattice) UserFunctions.live_f(next)).value());
		Set tmp89 = (Set) tmp95;
		return TransferFunction.assignEvalResult(direction, node, res, tmp89);
	}
}

class TransferFunction2 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(Analysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(term, 0), 0);
		Set result10 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result10.add(usrm);
			}
		}
		Set tmp93 = (Set) result10;
		Set tmp86 = (Set) tmp93;
		return TransferFunction.assignEvalResult(direction, node, res, tmp86);
	}
}

class TransferFunction3 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(Analysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
		Set result11 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result11.add(usrm);
			}
		}
		Set tmp92 = (Set) result11;
		Set tmp83 = (Set) tmp92;
		return TransferFunction.assignEvalResult(direction, node, res, tmp83);
	}
}

class TransferFunction4 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(Analysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node next = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Set result12 = new HashSet();
		for (Object usrm : (Set) ((FlockLattice) UserFunctions.live_f(next)).value()) {
			if (!usrm.equals(usrn)) {
				result12.add(usrm);
			}
		}
		Set tmp91 = (Set) result12;
		Set tmp80 = (Set) tmp91;
		return TransferFunction.assignEvalResult(direction, node, res, tmp80);
	}
}

class TransferFunction5 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(Analysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Set tmp77 = (Set) SetUtils.create();
		return TransferFunction.assignEvalResult(direction, node, res, tmp77);
	}
}

class UserFunctions {
	public static FlockLattice live_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("live").lattice;
	}
}