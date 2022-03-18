package org.metaborg.lang.tiger.flock.experiments.singlevalue;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockValueLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Graph.Node.NodeType;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.SingleAnalysis;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.experiments.singlevalue.SingleValueAnalysisProperties.ConstProp;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;

public class SingleValueAnalysis extends SingleAnalysis {
	public IStrategoTerm variable;

	public SingleValueAnalysis(IStrategoTerm variable, int id) {
		super("values_" + M.string(variable), Direction.FORWARD, id);
		this.variable = variable;
	}

	@Override
	public void initNodeValue(Node node) {
		node.addProperty(this.propertyName, Value.bottom());
	}

	@Override
	public void initNodeTransferFunction(Node node) {
		if (node.type != NodeType.NORMAL) {
			node.getProperty(this.propertyName).transfer = Passthrough;
			node.getProperty(this.propertyName).init = Start;
		} else {
			if (matchPattern0(node)) {
				node.getProperty(this.propertyName).transfer = TransferFunction0;
			}
			if (matchPattern1(node)) {
				node.getProperty(this.propertyName).transfer = TransferFunction1;
			}
			if (matchPattern2(node)) {
				node.getProperty(this.propertyName).transfer = TransferFunction2;
			}
			if (matchPattern3(node)) {
				node.getProperty(this.propertyName).transfer = TransferFunction3;
			}
			if (matchPattern4(node)) {
				node.getProperty(this.propertyName).transfer = TransferFunction4;
			}
			if (matchPattern5(node)) {
				node.getProperty(this.propertyName).transfer = TransferFunction5;
			}
			if (matchPattern6(node)) {
				node.getProperty(this.propertyName).init = TransferFunction6;
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

	public TransferFunction Passthrough = new Passthrough();
	public TransferFunction Start = new Start();
	public TransferFunction TransferFunction0 = new TransferFunction0();
	public TransferFunction TransferFunction1 = new TransferFunction1();
	public TransferFunction TransferFunction2 = new TransferFunction2();
	public TransferFunction TransferFunction3 = new TransferFunction3();
	public TransferFunction TransferFunction4 = new TransferFunction4();
	public TransferFunction TransferFunction5 = new TransferFunction5();
	public TransferFunction TransferFunction6 = new TransferFunction6();
	public UserFunctions UserFunctions = new UserFunctions();

	class Passthrough extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			return TransferFunction.assignEvalResult(direction, node, res, tmp57);
		}
	}

	class Start extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			Value tmp43 = (Value) Value.bottom();
			return TransferFunction.assignEvalResult(direction, node, res, tmp43);
		}
	}

	class TransferFunction0 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			return TransferFunction.assignEvalResult(direction, node, res, tmp57);
		}
	}

	class TransferFunction1 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			IStrategoTerm usrn = Helpers.at(Helpers.at(term, 0), 0);
			Value r = null;
			if (usrn.equals(variable)) {
				r = new Value(new ConstProp(
						factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null)));
			} else {
				r = tmp57;
			}
			return TransferFunction.assignEvalResult(direction, node, res, r);
		}
	}

	class TransferFunction2 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
			Value r = null;
			if (usrn.equals(variable)) {
				r = new Value(new ConstProp(
						factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null)));
			} else {
				r = tmp57;
			}
			return TransferFunction.assignEvalResult(direction, node, res, r);
		}
	}

	class TransferFunction3 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			IStrategoTerm usrn = Helpers.at(Helpers.at(Helpers.at(term, 0), 0), 0);
			IStrategoTerm usri = Helpers.at(Helpers.at(term, 1), 0);

			Value r = null;
			if (usrn.equals(variable)) {
				r = new Value(new ConstProp(factory.makeAppl(factory.makeConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null)));
			} else {
				r = tmp57;
			}
			return TransferFunction.assignEvalResult(direction, node, res, r);
		}
	}

	class TransferFunction4 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			IStrategoTerm usrn = Helpers.at(term, 0);
			Value r = null;
			if (usrn.equals(variable)) {
				r = new Value(new ConstProp(
						factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null)));
			} else {
				r = tmp57;
			}
			return TransferFunction.assignEvalResult(direction, node, res, r);
		}
	}

	class TransferFunction5 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			Node prev = node;
			Value tmp57 = (Value) UserFunctions.values_f(prev);
			IStrategoTerm usrn = Helpers.at(term, 0);
			IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);

			Value r = null;
			if (usrn.equals(variable)) {
				r = new Value(new ConstProp(factory.makeAppl(factory.makeConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null)));
			} else {
				r = tmp57;
			}
			return TransferFunction.assignEvalResult(direction, node, res, r);
		}
	}

	class TransferFunction6 extends TransferFunction {
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		@Override
		public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
			SimpleMap tmp43 = (SimpleMap) SimpleMap.bottom();
			return TransferFunction.assignEvalResult(direction, node, res, tmp43);
		}
	}

	class UserFunctions {
		public FlockLattice values_f(Object o) {
			Node node = (Node) o;
			return node.getProperty(propertyName).lattice;
		}
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
		ConstProp tmp73 = (ConstProp) new ConstProp(
				factory.makeAppl(factory.makeConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp73);
	}

	public static Value top() {
		ITermFactory factory = Flock.instance.factory;
		ConstProp tmp74 = (ConstProp) new ConstProp(
				factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null));
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
			result10 = new ConstProp(factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 1)) && (M.appl(Helpers.at(term4, 1)).getName().equals("Top")
						&& Helpers.at(term4, 1).getSubtermCount() == 0))) {
			result10 = new ConstProp(factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term4)
				&& (TermUtils.isAppl(Helpers.at(term4, 0)) && (M.appl(Helpers.at(term4, 0)).getName().equals("Const")
						&& (Helpers.at(term4, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term4, 1))
								&& (M.appl(Helpers.at(term4, 1)).getName().equals("Const")
										&& Helpers.at(term4, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term4, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term4, 1), 0);
			result10 = (boolean) usri.equals(usrj)
					? new ConstProp(factory.makeAppl(factory.makeConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))
					: new ConstProp(factory.makeAppl(factory.makeConstructor("Top", 0), new IStrategoTerm[] {}, null));
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
