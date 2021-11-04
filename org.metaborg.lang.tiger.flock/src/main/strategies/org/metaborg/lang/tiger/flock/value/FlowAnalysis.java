package org.metaborg.lang.tiger.flock.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockLatticeInPlace;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockValueLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.metaborg.lang.tiger.flock.value.FlowAnalysisProperties.ConstProp;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class FlowAnalysis extends Analysis {
	public FlowAnalysis() {
		super("values", Direction.FORWARD);
	}

	@Override
	public void initNodeValue(Node node) {
		node.addProperty("values", SimpleMap.bottom());
	}

	@Override
	public void initNodeTransferFunction(Node node) {
		{
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
				node.getProperty("values").init = TransferFunctions.TransferFunction3;
			}
		}
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
		ITerm term_0 = term.childAt(0);
		ITerm term_1 = term.childAt(1);
		ITerm term_2 = term.childAt(2);
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
		if (!(term_2.isAppl() && ((ApplTerm) term_2).getConstructor().equals("Int") && term_2.childrenCount() == 1)) {
			return false;
		}
		ITerm term_2_0 = term_2.childAt(0);
		return true;
	}

	private boolean matchPattern3(Node node) {
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
		ConstProp tmp82 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp82);
	}

	public static Value top() {
		ConstProp tmp83 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp83);
	}

	@Override
	public FlockLattice lub(FlockLattice other) {
		ConstProp usrl = ((Value) this).value;
		ConstProp usrr = ((Value) other).value;
		IStrategoTerm term3 = Helpers
				.toTerm(new StrategoTuple(new IStrategoTerm[] { Helpers.toTerm(usrl), Helpers.toTerm(usrr) }, null));
		Object result122 = null;
		if (TermUtils.isTuple(term3)
				&& (TermUtils.isAppl(Helpers.at(term3, 0)) && (M.appl(Helpers.at(term3, 0)).getName().equals("Top")
						&& Helpers.at(term3, 0).getSubtermCount() == 0))) {
			result122 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term3)
				&& (TermUtils.isAppl(Helpers.at(term3, 1)) && (M.appl(Helpers.at(term3, 1)).getName().equals("Top")
						&& Helpers.at(term3, 1).getSubtermCount() == 0))) {
			result122 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term3)
				&& (TermUtils.isAppl(Helpers.at(term3, 0)) && (M.appl(Helpers.at(term3, 0)).getName().equals("Const")
						&& (Helpers.at(term3, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term3, 1))
								&& (M.appl(Helpers.at(term3, 1)).getName().equals("Const")
										&& Helpers.at(term3, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term3, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term3, 1), 0);
			result122 = (boolean) usri.equals(usrj)
					? new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))
					: new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term3)
				&& (TermUtils.isAppl(Helpers.at(term3, 1)) && (M.appl(Helpers.at(term3, 1)).getName().equals("Bottom")
						&& Helpers.at(term3, 1).getSubtermCount() == 0))) {
			result122 = usrl;
		}
		if (TermUtils.isTuple(term3)
				&& (TermUtils.isAppl(Helpers.at(term3, 0)) && (M.appl(Helpers.at(term3, 0)).getName().equals("Bottom")
						&& Helpers.at(term3, 0).getSubtermCount() == 0))) {
			result122 = usrr;
		}
		if (result122 == null) {
			throw new RuntimeException("Could not match term");
		}
		ConstProp tmp84 = (ConstProp) result122;
		return new Value(tmp84);
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
		Node prev = node;
		SimpleMap tmp75 = (SimpleMap) UserFunctions.values_f(prev);
		return tmp75;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Map result123 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result123.put(usrk, usrv);
			}
		}
		Map tmp79 = (Map) result123;
		Map tmp80 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		Map tmp81 = (Map) new HashMap(MapUtils.union(tmp79, tmp80));
		Map tmp74 = (Map) tmp81;
		return new SimpleMap(tmp74);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);
		Map result124 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result124.put(usrk, usrv);
			}
		}
		Map tmp76 = (Map) result124;
		Map tmp77 = (Map) MapUtils.create(usrn,
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		Map tmp78 = (Map) new HashMap(MapUtils.union(tmp76, tmp77));
		Map tmp73 = (Map) tmp78;
		return new SimpleMap(tmp73);
	}
}

class TransferFunction3 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		SimpleMap tmp70 = (SimpleMap) SimpleMap.bottom();
		return tmp70;
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}