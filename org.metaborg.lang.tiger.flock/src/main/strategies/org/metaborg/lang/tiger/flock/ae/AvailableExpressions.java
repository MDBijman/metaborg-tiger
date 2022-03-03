package org.metaborg.lang.tiger.flock.ae;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MustSet;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.SetUtils;
import org.metaborg.lang.tiger.flock.common.SingleAnalysis;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TransferFunction;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class AvailableExpressions extends SingleAnalysis {
	public AvailableExpressions() {
		super("expressions", Direction.FORWARD);
	}

	@Override
	public void initNodeValue(Node node) {
		node.addProperty("expressions", SimpleMap.bottom());
	}

	@Override
	public void initNodeTransferFunction(Node node) {
		{
			if (matchPattern0(node)) {
				node.getProperty("expressions").transfer = TransferFunctions.TransferFunction0;
			}
			if (matchPattern1(node)) {
				node.getProperty("expressions").transfer = TransferFunctions.TransferFunction1;
			}
			if (matchPattern2(node)) {
				node.getProperty("expressions").init = TransferFunctions.TransferFunction2;
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
		return true;
	}
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
}

class TransferFunction0 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		SimpleMap tmp83 = (SimpleMap) UserFunctions.expressions_f(prev);
		return res.lub(tmp83);
	}
}

class TransferFunction1 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Node prev = node;
		IStrategoTerm usrn = Helpers.at(term, 0);
		IStrategoTerm usre = Helpers.at(term, 2);
		Set tmp85 = (Set) SetUtils.create(usrn);
		Map result201 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.expressions_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			result201.put(usrk, new MustSet(SetUtils.difference(((FlockLattice) usrv).value(), tmp85)));
		}
		Map tmp86 = (Map) result201;
		Set tmp87 = (Set) SetUtils.create(usrn);
		Map tmp88 = (Map) MapUtils.create(Helpers.toTerm(usre), new MustSet(tmp87));
		Map tmp89 = (Map) new HashMap(MapUtils.union(tmp86, tmp88));
		Map tmp82 = (Map) tmp89;
		return res.lub(new SimpleMap(tmp82));
	}
}

class TransferFunction2 extends TransferFunction {
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	public boolean eval(SingleAnalysis.Direction direction, FlockLattice res, Node node) {
		IStrategoTerm term = node.virtualTerm.toTermWithoutAnnotations();
		Map tmp81 = (Map) MapUtils.create();
		return res.lub(new SimpleMap(tmp81));
	}
}

class UserFunctions {
	public static FlockLattice expressions_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("expressions").lattice;
	}
}