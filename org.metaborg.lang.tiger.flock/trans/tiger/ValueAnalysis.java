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
import flock.subject.common.FlockLattice.MapLattice;
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

	public static void initNodeValue(Node node) {
		node.addProperty("values", Value.bottom());
	}

	public static void initNodeTransferFunction(Node node) {
		{
			if (true) {
				node.getProperty("values").transfer = TransferFunctions.TransferFunction0;
			}
			if (true) {
				node.getProperty("values").init = TransferFunctions.TransferFunction1;
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
		return new Value(new StrategoAppl(new StrategoConstructor("Bottom", 0), new IStrategoTerm[] {}, null));
	}

	public static Value top() {
		return new Value(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
	}

	@Override
	public FlockLattice lub(FlockLattice other) {
		ConstProp l_v = ((Value) this).value;
		IStrategoTerm l_t = (IStrategoTerm) l_v.toTerm();
		ConstProp r_v = ((Value) other).value;
		IStrategoTerm r_t = (IStrategoTerm) r_v.toTerm();
		IStrategoTerm term1 = Helpers
				.toTerm(new StrategoTuple(new IStrategoTerm[] { Helpers.toTerm(l), Helpers.toTerm(r) }, null));
		FlockLattice result1 = null;
		if (TermUtils.isTuple(term1)
				&& (TermUtils.isAppl(Helpers.at(term1, 0)) && (M.appl(Helpers.at(term1, 0)).getName().equals("Top")
						&& Helpers.at(term1, 0).getSubtermCount() == 0))) {
			result1 = new Value(
					new ConstProp(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null)));
		}
		if (TermUtils.isTuple(term1)
				&& (TermUtils.isAppl(Helpers.at(term1, 1)) && (M.appl(Helpers.at(term1, 1)).getName().equals("Top")
						&& Helpers.at(term1, 1).getSubtermCount() == 0))) {
			result1 = new Value(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term1)
				&& (TermUtils.isAppl(Helpers.at(term1, 0)) && (M.appl(Helpers.at(term1, 0)).getName().equals("Const")
						&& (Helpers.at(term1, 0).getSubtermCount() == 1 && (TermUtils.isAppl(Helpers.at(term1, 1))
								&& (M.appl(Helpers.at(term1, 1)).getName().equals("Const")
										&& Helpers.at(term1, 1).getSubtermCount() == 1)))))) {
			IStrategoTerm i_t = Helpers.at(Helpers.at(term1, 0), 0);
			IStrategoTerm j_t = Helpers.at(Helpers.at(term1, 1), 0);
			result1 = (boolean) i.equals(j)
					? new Value(new StrategoAppl(new StrategoConstructor("Const", 1),
							new IStrategoTerm[] { Helpers.toTerm(i) }, null))
					: new Value(new StrategoAppl(new StrategoConstructor("Top", 0), new IStrategoTerm[] {}, null));
		}
		if (TermUtils.isTuple(term1)
				&& (TermUtils.isAppl(Helpers.at(term1, 1)) && (M.appl(Helpers.at(term1, 1)).getName().equals("Bottom")
						&& Helpers.at(term1, 1).getSubtermCount() == 0))) {
			result1 = new Value(l);
		}
		if (TermUtils.isTuple(term1)
				&& (TermUtils.isAppl(Helpers.at(term1, 0)) && (M.appl(Helpers.at(term1, 0)).getName().equals("Bottom")
						&& Helpers.at(term1, 0).getSubtermCount() == 0))) {
			result1 = new Value(r);
		}
		if (result1 == null) {
			throw new RuntimeException("Could not match term");
		}
		return result1;
	}
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
}

class TransferFunction0 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		Node prev_t = node;
		return UserFunctions.values_f(prev_t);
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		IStrategoTerm term = node.term;
		return Value.top();
	}
}

class UserFunctions {
	public static FlockLattice values_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("values").lattice;
	}
}