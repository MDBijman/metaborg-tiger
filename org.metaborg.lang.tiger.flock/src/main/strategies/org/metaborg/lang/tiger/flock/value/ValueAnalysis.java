package org.metaborg.lang.tiger.flock.value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.Analysis.Direction;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockValueLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.metaborg.lang.tiger.flock.value.ValueAnalysisProperties.ConstProp;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class ValueAnalysis extends Analysis {
	public ValueAnalysis() {
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
		ConstProp tmp72 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp72);
	}

	public static Value top() {
		ConstProp tmp73 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp73);
	}

	@Override
	public boolean lub(FlockLattice other) {
		ConstProp usrl = ((Value) this).value;
		ConstProp usrr = ((Value) other).value;
		IStrategoTerm term8 = Helpers
				.toTerm(new StrategoTuple(new IStrategoTerm[] { Helpers.toTerm(usrl), Helpers.toTerm(usrr) }, null));
		Object result149 = null;
		if (TermUtils.isTuple(term8)
				&& (TermUtils.isAppl(Helpers.at(term8, 0)) && (M.appl(Helpers.at(term8, 0)).getName().equals("Top")
						&& Helpers.at(term8, 0).getSubtermCount() == 0))) {
			result149 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term8)
				&& (TermUtils.isAppl(Helpers.at(term8, 1)) && (M.appl(Helpers.at(term8, 1)).getName().equals("Top")
						&& Helpers.at(term8, 1).getSubtermCount() == 0))) {
			result149 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term8)
				&& (TermUtils.isAppl(Helpers.at(term8, 0)) && (M.appl(Helpers.at(term8, 0)).getName().equals("Const")
						&& (Helpers.at(term8, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term8, 1))
								&& (M.appl(Helpers.at(term8, 1)).getName().equals("Const")
										&& Helpers.at(term8, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term8, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term8, 1), 0);
			result149 = (boolean) usri.equals(usrj)
					? new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))
					: new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term8)
				&& (TermUtils.isAppl(Helpers.at(term8, 1)) && (M.appl(Helpers.at(term8, 1)).getName().equals("Bottom")
						&& Helpers.at(term8, 1).getSubtermCount() == 0))) {
			result149 = usrl;
		}
		if (TermUtils.isTuple(term8)
				&& (TermUtils.isAppl(Helpers.at(term8, 0)) && (M.appl(Helpers.at(term8, 0)).getName().equals("Bottom")
						&& Helpers.at(term8, 0).getSubtermCount() == 0))) {
			result149 = usrr;
		}
		if (result149 == null) {
			throw new RuntimeException("Could not match term");
		}
		ConstProp tmp74 = (ConstProp) result149;
		Value _res_lat = new Value(tmp74);
		boolean _is_changed = !this.equals(_res_lat);
		this.value = _res_lat.value;
		return _is_changed;
	}

	@Override
	public FlockLattice copy() {
		return new Value(this);
	}
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
	public static TransferFunction TransferFunction3 = new TransferFunction3();
	public static TransferFunction TransferFunction4 = new TransferFunction4();
	public static TransferFunction TransferFunction5 = new TransferFunction5();
	public static TransferFunction TransferFunction6 = new TransferFunction6();
}

class TransferFunction0 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		SimpleMap tmp56 = (SimpleMap) UserFunctions.values_f(prev);
		return res.lub(tmp56);
	}
}

class TransferFunction1 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(term, 0), 0);
		Map result150 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result150.put(usrk, usrv);
			}
		}
		Map tmp69 = (Map) result150;
		Map tmp70 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		MapUtils.unionInplace(tmp69, tmp70);
		return res.lub(new SimpleMap(tmp69));
	}
}

class TransferFunction2 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
		Map result151 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result151.put(usrk, usrv);
			}
		}
		Map tmp66 = (Map) result151;
		Map tmp67 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		Map tmp68 = (Map) new HashMap(MapUtils.union(tmp66, tmp67));
		Map tmp54 = (Map) tmp68;
		return res.lub(new SimpleMap(tmp54));
	}
}

class TransferFunction3 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 1), 0);
		Map result152 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result152.put(usrk, usrv);
			}
		}
		Map tmp63 = (Map) result152;
		Map tmp64 = (Map) MapUtils.create(usrn,
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		Map tmp65 = (Map) new HashMap(MapUtils.union(tmp63, tmp64));
		Map tmp53 = (Map) tmp65;
		return res.lub(new SimpleMap(tmp53));
	}
}

class TransferFunction4 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Map result153 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result153.put(usrk, usrv);
			}
		}
		Map tmp60 = (Map) result153;
		Map tmp61 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		Map tmp62 = (Map) new HashMap(MapUtils.union(tmp60, tmp61));
		Map tmp52 = (Map) tmp62;
		return res.lub(new SimpleMap(tmp52));
	}
}

class TransferFunction5 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);
		Map result154 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result154.put(usrk, usrv);
			}
		}
		Map tmp57 = (Map) result154;
		Map tmp58 = (Map) MapUtils.create(usrn,
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		MapUtils.unionInplace(tmp57, tmp58);
		return res.lub(new SimpleMap(tmp57));
	}
}

class TransferFunction6 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		SimpleMap tmp50 = (SimpleMap) SimpleMap.bottom();
		return res.lub(tmp50);
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}