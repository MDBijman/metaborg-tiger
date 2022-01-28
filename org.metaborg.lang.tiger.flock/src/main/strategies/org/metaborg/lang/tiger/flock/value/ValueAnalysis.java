package org.metaborg.lang.tiger.flock.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.Flock;
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
		ConstProp tmp107 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp107);
	}

	public static Value top() {
		ConstProp tmp108 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp108);
	}

	@Override
	public FlockLattice lub(FlockLattice other) {
		ConstProp usrl = ((Value) this).value;
		ConstProp usrr = ((Value) other).value;
		IStrategoTerm term11 = Helpers
				.toTerm(new StrategoTuple(new IStrategoTerm[] { Helpers.toTerm(usrl), Helpers.toTerm(usrr) }, null));
		Object result176 = null;
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 0)) && (M.appl(Helpers.at(term11, 0)).getName().equals("Top")
						&& Helpers.at(term11, 0).getSubtermCount() == 0))) {
			result176 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 1)) && (M.appl(Helpers.at(term11, 1)).getName().equals("Top")
						&& Helpers.at(term11, 1).getSubtermCount() == 0))) {
			result176 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 0)) && (M.appl(Helpers.at(term11, 0)).getName().equals("Const")
						&& (Helpers.at(term11, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term11, 1))
								&& (M.appl(Helpers.at(term11, 1)).getName().equals("Const")
										&& Helpers.at(term11, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term11, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term11, 1), 0);
			result176 = (boolean) usri.equals(usrj)
					? new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))
					: new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 1)) && (M.appl(Helpers.at(term11, 1)).getName().equals("Bottom")
						&& Helpers.at(term11, 1).getSubtermCount() == 0))) {
			result176 = usrl;
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 0)) && (M.appl(Helpers.at(term11, 0)).getName().equals("Bottom")
						&& Helpers.at(term11, 0).getSubtermCount() == 0))) {
			result176 = usrr;
		}
		if (result176 == null) {
			throw new RuntimeException("Could not match term");
		}
		ConstProp tmp109 = (ConstProp) result176;
		return new Value(tmp109);
	}

	@Override
	public boolean lubInplace(FlockLattice other) {
		ConstProp usrl = ((Value) this).value;
		ConstProp usrr = ((Value) other).value;
		IStrategoTerm term11 = Helpers
				.toTerm(new StrategoTuple(new IStrategoTerm[] { Helpers.toTerm(usrl), Helpers.toTerm(usrr) }, null));
		Object result176 = null;
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 0)) && (M.appl(Helpers.at(term11, 0)).getName().equals("Top")
						&& Helpers.at(term11, 0).getSubtermCount() == 0))) {
			result176 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 1)) && (M.appl(Helpers.at(term11, 1)).getName().equals("Top")
						&& Helpers.at(term11, 1).getSubtermCount() == 0))) {
			result176 = new ConstProp(
					new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 0)) && (M.appl(Helpers.at(term11, 0)).getName().equals("Const")
						&& (Helpers.at(term11, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term11, 1))
								&& (M.appl(Helpers.at(term11, 1)).getName().equals("Const")
										&& Helpers.at(term11, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term11, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term11, 1), 0);
			result176 = (boolean) usri.equals(usrj)
					? new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))
					: new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 1)) && (M.appl(Helpers.at(term11, 1)).getName().equals("Bottom")
						&& Helpers.at(term11, 1).getSubtermCount() == 0))) {
			result176 = usrl;
		}
		if (TermUtils.isTuple(term11)
				&& (TermUtils.isAppl(Helpers.at(term11, 0)) && (M.appl(Helpers.at(term11, 0)).getName().equals("Bottom")
						&& Helpers.at(term11, 0).getSubtermCount() == 0))) {
			result176 = usrr;
		}
		if (result176 == null) {
			throw new RuntimeException("Could not match term");
		}

		ConstProp tmp109 = (ConstProp) result176;
		boolean changed = !result176.equals(this.value);
		this.value = tmp109;
		return changed;
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
	@Override
	public FlockLattice eval(Node node) {
		Node prev = node;
		SimpleMap tmp91 = (SimpleMap) UserFunctions.values_f(prev);
		return tmp91;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(term, 0), 0);
		Map result177 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result177.put(usrk, usrv);
			}
		}
		Map tmp104 = (Map) result177;
		Map tmp105 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		MapUtils.unionInplace(tmp104, tmp105);
		return new SimpleMap(tmp104);
	}

	@Override
	public boolean supportsInplace() {
		return true;
	}

	@Override
	public boolean evalInplace(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);

		Map resValues = (Map) ((SimpleMap) res).value();

		boolean changed = false;
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				if (resValues.containsKey(usrk)) {
					changed |= ((FlockLattice) resValues.get(usrk)).lubInplace((FlockLattice) usrv);
				} else {
					resValues.put(usrk, usrv);
					changed = true;
				}
			}
		}

		if (resValues.containsKey(usrn)) {
			changed |= ((FlockLattice) resValues.get(usrn))
					.lubInplace(new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		} else {
			resValues.put(usrn, new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
					new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
			changed = true;
		}

		return changed;
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
		Map result178 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result178.put(usrk, usrv);
			}
		}
		Map tmp101 = (Map) result178;
		Map tmp102 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		MapUtils.unionInplace(tmp101, tmp102);
		return new SimpleMap(tmp101);
	}
}

class TransferFunction3 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 1), 0);
		Map result179 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result179.put(usrk, usrv);
			}
		}
		Map tmp98 = (Map) result179;
		Map tmp99 = (Map) MapUtils.create(usrn,
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		MapUtils.unionInplace(tmp98, tmp99);
		return new SimpleMap(tmp98);
	}
}

class TransferFunction4 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Map result180 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result180.put(usrk, usrv);
			}
		}
		Map tmp95 = (Map) result180;
		Map tmp96 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		MapUtils.unionInplace(tmp95, tmp96);
		return new SimpleMap(tmp95);
	}
}

class TransferFunction5 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);
		Map result181 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result181.put(usrk, usrv);
			}
		}

		Map tmp92 = (Map) result181;

		Map tmp93 = (Map) MapUtils.create(usrn,
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		MapUtils.unionInplace(tmp92, tmp93);
		return new SimpleMap(tmp92);

	}

	@Override
	public boolean supportsInplace() {
		return true;
	}

	@Override
	public boolean evalInplace(FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);

		Map resValues = (Map) ((SimpleMap) res).value();

		boolean changed = false;
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				if (resValues.containsKey(usrk)) {
					changed |= ((FlockLattice) resValues.get(usrk)).lubInplace((FlockLattice) usrv);
				} else {
					resValues.put(usrk, usrv);
					changed = true;
				}
			}
		}

		if (resValues.containsKey(usrn)) {
			changed |= ((FlockLattice) resValues.get(usrn))
					.lubInplace(new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		} else {
			resValues.put(usrn, new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
					new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
			changed = true;
		}

		return changed;
	}
}

class TransferFunction6 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		SimpleMap tmp77 = (SimpleMap) SimpleMap.bottom();
		return tmp77;
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}