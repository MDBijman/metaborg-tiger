package org.metaborg.lang.tiger.flock.ae;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.metaborg.lang.tiger.flock.common.Analysis;
import org.metaborg.lang.tiger.flock.common.FlockLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MustSet;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.Graph;
import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.metaborg.lang.tiger.flock.common.MapUtils;
import org.metaborg.lang.tiger.flock.common.SCCs;
import org.metaborg.lang.tiger.flock.common.SetUtils;
import org.metaborg.lang.tiger.flock.common.TermTree.ApplTerm;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.metaborg.lang.tiger.flock.common.TransferFunction;

public class AvailableExpressions extends Analysis {
	public AvailableExpressions() {
		super("expressions", Direction.FORWARD);
	}

	public void initNodeValue(Node node) {
		node.addProperty("expressions", SimpleMap.bottom());
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
		return true;
	}

	private boolean matchPattern2(Node node) {
		ITerm term = node.virtualTerm;
		return true;
	}

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
}

class TransferFunctions {
	public static TransferFunction TransferFunction0 = new TransferFunction0();
	public static TransferFunction TransferFunction1 = new TransferFunction1();
	public static TransferFunction TransferFunction2 = new TransferFunction2();
}

class TransferFunction0 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Node prev = node;
		SimpleMap tmp68 = (SimpleMap) UserFunctions.expressions_f(prev);
		return tmp68;
	}
}

class TransferFunction1 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Node prev = node;
		ITerm usrn = term.childAt(0);
		ITerm usre = term.childAt(2);
		Set tmp69 = (Set) SetUtils.create(usrn);
		Map result10 = new HashMap();
		for (Object o : ((Map) ((FlockLattice) UserFunctions.expressions_f(prev)).value()).entrySet()) {
			Entry entry = (Entry) o;
			Object usrk = entry.getKey();
			Object usrv = entry.getValue();
			result10.put(usrk, new MustSet(SetUtils.difference(((FlockLattice) usrv).value(), tmp69)));
		}
		Map tmp70 = (Map) result10;
		Set tmp71 = (Set) SetUtils.create(usrn.toTerm());
		Map tmp72 = (Map) MapUtils.create(Helpers.toTerm(usre.toTerm()), new MustSet(tmp71));
		Map tmp81 = (Map) MapUtils.union(tmp70, tmp72);
		Map tmp67 = (Map) tmp81;
		return new SimpleMap(tmp67);
	}
}

class TransferFunction2 extends TransferFunction {
	@Override
	public FlockLattice eval(Node node) {
		ITerm term = node.virtualTerm;
		Map tmp66 = (Map) MapUtils.create();
		return new SimpleMap(tmp66);
	}
}

class UserFunctions {
	public static FlockLattice expressions_f(Object o) {
		Node node = (Node) o;
		return node.getProperty("expressions").lattice;
	}
}