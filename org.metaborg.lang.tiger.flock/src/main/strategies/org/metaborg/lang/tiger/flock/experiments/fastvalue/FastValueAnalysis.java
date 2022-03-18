package org.metaborg.lang.tiger.flock.experiments.fastvalue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockValueLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.SingleAnalysis;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.experiments.fastvalue.FastValueAnalysisProperties.*;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.NotImplementedException;
import org.spoofax.terms.util.TermUtils;

public class FastValueAnalysis extends SingleAnalysis {
	public FastValueAnalysis(int id) {
		super("values", Direction.FORWARD, id);
	}

	@Override
	public void initNodeValue(Node node) {
		node.addProperty("values", SimpleMap.bottom());
	}

	@Override
	public void initNodeTransferFunction(Node node) {
		if (node.type != NodeType.NORMAL) {
			node.getProperty("values").transfer = TransferFunctions.Passthrough;
			node.getProperty("values").init = TransferFunctions.Start;
		} else {
			if (matchPattern0(node)) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction0;
			}
			if (matchPattern1(node)) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction1;
			}
			if (matchPattern2(node)) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction2;
			}
			if (matchPattern3(node)) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction3;
			}
			if (matchPattern4(node)) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction4;
			}
			if (matchPattern5(node)) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction5;
			}
			if (matchPattern6(node)) {
				node.getProperty("values").init = TransferFunctions.TransferFunction6;
			}
		}
	}

	private boolean matchPattern0(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}

	private boolean matchPattern1(Node node) {
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

	private boolean matchPattern2(Node node) {
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
		if (!(term_1.isAppl() && ((ApplTerm) term_1).getConstructor().equals("Int") && term_1.childrenCount() == 1)) {
			return false;
		}
		ITerm term_1_0 = term_1.childAt(0);
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
		if (!(term.isAppl() && ((ApplTerm) term).getConstructor().equals("VarDec") && term.childrenCount() == 3)) {
			return false;
		}
		ITerm term_0 = term.childAt(0);
		ITerm term_1 = term.childAt(1);
		ITerm term_2 = term.childAt(2);
		if (!(term_2.isAppl() && ((ApplTerm) term_2).getConstructor().equals("Int") && term_2.childrenCount() == 1)) {
			return false;
		}
		ITerm term_2_0 = term_2.childAt(0);
		return true;
	}

	private boolean matchPattern6(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}
}

class Value implements FlockValueLattice {
	ConstProp value;

	public Value(Value o) {
		this.value = o.value;
	}

	public Value(ConstProp v) {
		this.value = v;
	}

	@Override
	public FlockValue value() {
		return this.value;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != this.getClass())
			return false;
		Value rhs = (Value) other;
		return this.value.equals(rhs.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public static Value bottom() {
		ITermFactory factory = Flock.instance.factory;
		ConstProp tmp73 = (ConstProp) new ConstPropBottom();
		return new Value(tmp73);
	}

	public static Value top() {
		ITermFactory factory = Flock.instance.factory;
		ConstProp tmp74 = (ConstProp) new ConstPropTop();
		return new Value(tmp74);
	}

	@Override
	public boolean lub(Object other) {
		ITermFactory factory = Flock.instance.factory;
		ConstProp usrl = ((Value) this).value;
		ConstProp usrr = ((Value) other).value;
		IStrategoTerm term4 = Helpers
				.toTerm(factory.makeTuple(new IStrategoTerm[] { Helpers.toTerm(usrl), Helpers.toTerm(usrr) }, null));
		Object result10 = null;
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 0)) && (M.appl(Helpers.at(term4, 0)).getName().equals("Top")
						&& Helpers.at(term4, 0).getSubtermCount() == 0))) {
			result10 = new ConstPropTop();
		}
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 1)) && (M.appl(Helpers.at(term4, 1)).getName().equals("Top")
						&& Helpers.at(term4, 1).getSubtermCount() == 0))) {
			result10 = new ConstPropTop();
		}
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 0)) && (M.appl(Helpers.at(term4, 0)).getName().equals("Const")
						&& (Helpers.at(term4, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term4, 1))
								&& (M.appl(Helpers.at(term4, 1)).getName().equals("Const")
										&& Helpers.at(term4, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term4, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term4, 1), 0);
			result10 = (boolean) usri.equals(usrj) ? new ConstPropConst(Integer.parseInt(M.string(usri))) : new ConstPropTop();
		}
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 1)) && (M.appl(Helpers.at(term4, 1)).getName().equals("Bottom")
						&& Helpers.at(term4, 1).getSubtermCount() == 0))) {
			result10 = usrl;
		}
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 0)) && (M.appl(Helpers.at(term4, 0)).getName().equals("Bottom")
						&& Helpers.at(term4, 0).getSubtermCount() == 0))) {
			result10 = usrr;
		}
		if (result10 == null) {
			throw new RuntimeException("Could not match term");
		}
		ConstProp tmp75 = (ConstProp) result10;
		Value _res_lat = new Value(tmp75);
		boolean _is_changed = !this.equals(_res_lat);
		this.value = _res_lat.value;
		return _is_changed;
	}

	@Override
	public FlockLattice copy() {
		return new Value(this);
	}

	@Override
	public void setValue(Object value) {
		this.value = (ConstProp) value;
	}
}

class TransferFunctions {
	public static TransferFunction Passthrough = new Passthrough();
	public static TransferFunction Start = new Start();
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
	public static TransferFunction TransferFunction3 = new TransferFunction3();
	public static TransferFunction TransferFunction4 = new TransferFunction4();
	public static TransferFunction TransferFunction5 = new TransferFunction5();
	public static TransferFunction TransferFunction6 = new TransferFunction6();
}

class Passthrough extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		ITermFactory factory = Flock.instance.factory;
		Node prev = node;
		SimpleMap tmp57 = (SimpleMap) UserFunctions.values_f(prev);
		return TransferFunction.assignEvalResult(direction, node, res, tmp57);
	}
}

class Start extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		SimpleMap tmp43 = (SimpleMap) SimpleMap.bottom();
		return TransferFunction.assignEvalResult(direction, node, res, tmp43);
	}
}

class TransferFunction0 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		Flock.increment("tf0");
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		SimpleMap tmp57 = (SimpleMap) UserFunctions.values_f(prev);
		return TransferFunction.assignEvalResult(direction, node, res, tmp57);
	}
}

class TransferFunction1 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		Flock.increment("tf1");
		throw new NotImplementedException();
	}
}

class TransferFunction2 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		Flock.increment("tf2");
		throw new NotImplementedException();
	}
}

class TransferFunction3 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		Flock.increment("tf3");
		throw new NotImplementedException();
	}
}

class TransferFunction4 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		throw new NotImplementedException();

	}
}

class TransferFunction5 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		Flock.increment("tf5");
		//long t = System.nanoTime();
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;

		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);
		Map x = ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value());
		Map result = new HashMap<>(x);
		result.put(usrn, new Value(new ConstPropConst(Integer.parseInt(M.string(usri)))));
		//Flock.printDebug("" + ((System.nanoTime() - t)/100000.0d));
		return TransferFunction.assignEvalResult(direction, node, res, result);
	}
}

class TransferFunction6 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		Flock.increment("tf6");
		ITermFactory factory = Flock.instance.factory;
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		SimpleMap tmp43 = (SimpleMap) SimpleMap.bottom();
		return TransferFunction.assignEvalResult(direction, node, res, tmp43);
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}