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
		super("values", Direction.FORWARD);
	}

	public void initNodeValue(Node node) {
		node.addProperty("values", SimpleMap.bottom());
	}

	private boolean matchPattern0(Node node) {
		IStrategoTerm term = node.term;
		return true;
	}

	private boolean matchPattern1(Node node) {
		IStrategoTerm term = node.term;
		if (!(TermUtils.isAppl(term) && M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			return false;
		}
		IStrategoTerm term_0 = Helpers.at(term, 0);
		IStrategoTerm term_1 = Helpers.at(term, 1);
		IStrategoTerm term_2 = Helpers.at(term, 2);
		addNodePatternParent(node, Helpers.getTermNode(term));
		return true;
	}

	private boolean matchPattern2(Node node) {
		IStrategoTerm term = node.term;
		if (!(TermUtils.isAppl(term) && M.appl(term).getName().equals("VarDec") && term.getSubtermCount() == 3)) {
			return false;
		}
		IStrategoTerm term_0 = Helpers.at(term, 0);
		IStrategoTerm term_1 = Helpers.at(term, 1);
		IStrategoTerm term_2 = Helpers.at(term, 2);
		if (!(TermUtils.isAppl(term_2) && M.appl(term_2).getName().equals("Int") && term_2.getSubtermCount() == 1)) {
			return false;
		}
		IStrategoTerm term_2_0 = Helpers.at(term_2, 0);
		addNodePatternParent(node, Helpers.getTermNode(term));
		addNodePatternParent(node, Helpers.getTermNode(term_2));
		return true;
	}

	private boolean matchPattern3(Node node) {
		IStrategoTerm term = node.term;
		return true;
	}

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
			root.getProperty("values").lattice = root.getProperty("values").init.eval(root);
			this.changedNodes.add(root);
		}
		for (Node node : nodeset) {
			if (node.interval > intervalBoundary)
				continue;
			{
				FlockLattice init = node.getProperty("values").lattice;
				for (Node pred : g.parentsOf(node)) {
					FlockLattice live_o = pred.getProperty("values").transfer.eval(pred);
					init = init.lub(live_o);
				}
				node.getProperty("values").lattice = init;
				this.changedNodes.add(node);
			}
		}
		while (!worklist.isEmpty()) {
			Node node = worklist.poll();
			inWorklist.remove(node);
			if (node.interval > intervalBoundary)
				continue;
			FlockLattice values_n = node.getProperty("values").transfer.eval(node);
			for (Node successor : g.childrenOf(node)) {
				if (successor.interval > intervalBoundary)
					continue;
				boolean changed = false;
				FlockLattice values_o = successor.getProperty("values").lattice;
				if (values_n.nleq(values_o)) {
					successor.getProperty("values").lattice = values_o.lub(values_n);
					changed = true;
				}
				FlockLattice values_o = successor.getProperty("values").lattice;
				if (values_n.nleq(values_o)) {
					successor.getProperty("values").lattice = values_o.lub(values_n);
					changed = true;
				}
				FlockLattice values_o = successor.getProperty("values").lattice;
				if (values_n.nleq(values_o)) {
					successor.getProperty("values").lattice = values_o.lub(values_n);
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
			for (Node successor : g.parentsOf(node)) {
				boolean changed = false;
				if (successor.interval > intervalBoundary)
					continue;
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
	public String toString() {
		return value.toString();
	}

	public static Value bottom() {
		ConstProp tmp18 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp18);
	}

	public static Value top() {
		ConstProp tmp19 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp19);
	}

	@Override
	public FlockLattice lub(FlockLattice other) {
		ConstProp usrl = ((Value) this).value;
		ConstProp usrr = ((Value) other).value;
		IStrategoTerm term0 = Helpers
				.toTerm(new StrategoTuple(new IStrategoTerm[] { Helpers.toTerm(usrl), Helpers.toTerm(usrr) }, null));
		Object result0 = null;
		if (TermUtils.isTuple(term0)
				&& (TermUtils.isAppl(Helpers.at(term0, 0)) && (M.appl(Helpers.at(term0, 0)).getName().equals("Top")
						&& Helpers.at(term0, 0).getSubtermCount() == 0))) {
			result0 = new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term0)
				&& (TermUtils.isAppl(Helpers.at(term0, 1)) && (M.appl(Helpers.at(term0, 1)).getName().equals("Top")
						&& Helpers.at(term0, 1).getSubtermCount() == 0))) {
			result0 = new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term0)
				&& (TermUtils.isAppl(Helpers.at(term0, 0)) && (M.appl(Helpers.at(term0, 0)).getName().equals("Const")
						&& (Helpers.at(term0, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term0, 1))
								&& (M.appl(Helpers.at(term0, 1)).getName().equals("Const")
										&& Helpers.at(term0, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm usri = Helpers.at(Helpers.at(term0, 0), 0);
			IStrategoTerm usrj = Helpers.at(Helpers.at(term0, 1), 0);
			result0 = (boolean) usri.equals(usrj)
					? new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(usri) }, null))
					: new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term0)
				&& (TermUtils.isAppl(Helpers.at(term0, 1)) && (M.appl(Helpers.at(term0, 1)).getName().equals("Bottom")
						&& Helpers.at(term0, 1).getSubtermCount() == 0))) {
			result0 = usrl;
		}
		if (TermUtils.isTuple(term0)
				&& (TermUtils.isAppl(Helpers.at(term0, 0)) && (M.appl(Helpers.at(term0, 0)).getName().equals("Bottom")
						&& Helpers.at(term0, 0).getSubtermCount() == 0))) {
			result0 = usrr;
		}
		if (result0 == null) {
			throw new RuntimeException("Could not match term");
		}
		ConstProp tmp20 = (ConstProp) result0;
		return new Value(tmp20);
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
		Node prev = node;
		SimpleMap tmp11 = (SimpleMap) UserFunctions.values_f(prev);
		return tmp11;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		Map result1 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result1.put(usrk, usrv);
			}
		}
		Map tmp15 = (Map) result1;
		Map tmp16 = (Map) MapUtils.create(usrn, new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		Map tmp17 = (Map) new HashMap(MapUtils.union(tmp15, tmp16));
		Map tmp10 = (Map) tmp17;
		return new SimpleMap(tmp10);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usri = Helpers.at(Helpers.at(term, 2), 0);
		Map result2 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn)) {
				result2.put(usrk, usrv);
			}
		}
		Map tmp12 = (Map) result2;
		Map tmp13 = (Map) MapUtils.create(usrn,
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { Helpers.toTerm(usri) }, null))));
		Map tmp14 = (Map) new HashMap(MapUtils.union(tmp12, tmp13));
		Map tmp9 = (Map) tmp14;
		return new SimpleMap(tmp9);
	}
}

class TransferFunction3 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		SimpleMap tmp6 = (SimpleMap) SimpleMap.bottom();
		return tmp6;
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}