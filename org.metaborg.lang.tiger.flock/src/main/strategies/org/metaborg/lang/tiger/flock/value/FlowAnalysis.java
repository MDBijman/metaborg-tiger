package org.metaborg.lang.tiger.flock.value;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockValueLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
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

	public void initNodeValue(Node node) {
		node.addProperty("values", SimpleMap.bottom());
	}

	private boolean matchPattern0(Node node) {
		return true;
	}

	private boolean matchPattern1(Node node) {
		ITerm vnode = node.virtualTerm;
		if (!(vnode.isAppl() && ((ApplTerm) vnode).getConstructor().equals("VarDec")
				&& node.virtualTerm.childrenCount() == 3)) {
			return false;
		}
		return true;
	}

	private boolean matchPattern2(Node node) {
		ITerm vnode = node.virtualTerm;
		if (!(vnode.isAppl() && ((ApplTerm) vnode).getConstructor().equals("VarDec") && vnode.childrenCount() == 3)) {
			return false;
		}
		ITerm vnode_2 = vnode.childAt(2);
		if (!(vnode_2.isAppl() && ((ApplTerm) vnode_2).getConstructor().equals("Int")
				&& vnode_2.childrenCount() == 1)) {
			return false;
		}
		return true;
	}

	private boolean matchPattern3(Node node) {
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
			}
		}
		HashSet<Node> keepDirty = new HashSet<>();
		while (!worklist.isEmpty()) {
			Node node = worklist.poll();
			inWorklist.remove(node);
			if (node.interval > intervalBoundary)
				continue;
			FlockLattice values_n = node.getProperty("values").transfer.eval(node);
			for (Node successor : g.childrenOf(node)) {
				if (successor.interval > intervalBoundary) {
					keepDirty.add(node);
					continue;
				}
				boolean changed = false;
				FlockLattice values_o = successor.getProperty("values").lattice;
				if (values_n.nleq(values_o)) {
					successor.getProperty("values").lattice = values_o.lub(values_n);
					changed = true;
				}
				if (changed && !inWorklist.contains(successor)) {
					worklist.add(successor);
					inWorklist.add(successor);
				}
			}
		}

		// Remove the updated nodes from the dirty/new collections
		this.dirtyNodes.removeIf(n -> !keepDirty.contains(n) && this.withinBoundary(g.intervalOf(n), intervalBoundary));
		this.newNodes.removeIf(n -> this.withinBoundary(g.intervalOf(n), intervalBoundary));
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
		ConstProp tmp10 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp10);
	}

	public static Value top() {
		ConstProp tmp11 = (ConstProp) new ConstProp(
				new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		return new Value(tmp11);
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
		ConstProp tmp12 = (ConstProp) result0;
		return new Value(tmp12);
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
		ITerm term = node.virtualTerm;
		Node prev = node;
		SimpleMap tmp3 = (SimpleMap) UserFunctions.values_f(prev);
		return tmp3;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Node prev = node;
		ITerm usrn = term.childAt(0);
		Map result1 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn.toTermWithoutAnnotations())) {
				result1.put(usrk, usrv);
			}
		}
		Map tmp7 = (Map) result1;
		Map tmp8 = (Map) MapUtils.create(usrn.toTermWithoutAnnotations(), new Value(
				new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null))));
		Map tmp9 = (Map) new HashMap(MapUtils.union(tmp7, tmp8));
		Map tmp2 = (Map) tmp9;
		return new SimpleMap(tmp2);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Node prev = node;
		ITerm usrn = term.childAt(0);
		ITerm usri = term.childAt(2).childAt(0);
		Map result2 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.values_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			if (!usrk.equals(usrn.toTermWithoutAnnotations())) {
				result2.put(usrk, usrv);
			}
		}
		Map tmp4 = (Map) result2;
		Map tmp5 = (Map) MapUtils.create(usrn.toTermWithoutAnnotations(),
				new Value(new ConstProp(new StrategoAppl(new StrategoConstructor("Const", 1),
						new IStrategoTerm[] { usri.toTermWithoutAnnotations() }, null))));
		Map tmp6 = (Map) new HashMap(MapUtils.union(tmp4, tmp5));
		Map tmp1 = (Map) tmp6;
		return new SimpleMap(tmp1);
	}
}

class TransferFunction3 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		SimpleMap tmp0 = (SimpleMap) SimpleMap.bottom();
		return tmp0;
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}