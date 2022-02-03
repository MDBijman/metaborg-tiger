 package org.metaborg.lang.tiger.flock.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.Graph.Node;
import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

public class Helpers {
	public static IStrategoTerm toTerm(Object o) {
		if (o instanceof IStrategoTerm) {
			return (IStrategoTerm) o;
		}
		if (o instanceof Integer) {
			return new StrategoInt((int) o);
		}
		if (o instanceof String) {
			return new StrategoString((String) o, null);
		}
		if (o instanceof Set) {
			Set s = (Set) o;
			Object[] elements = s.toArray();
			IStrategoList result = new StrategoList(toTerm(elements[elements.length - 1]), null, null);
			for (int i = elements.length - 2; i >= 0; i--) {
				result = new StrategoList(toTerm(elements[i]), result, null);
			}
			return result;
		}
		if (o instanceof HashMap) {
			HashMap m = (HashMap) o;
			IStrategoList result = new StrategoList(null, null, null);
			for (Object eo : m.entrySet()) {
				Entry e = (Entry) eo;

				IStrategoTerm[] pair = { toTerm(e.getKey()), toTerm(e.getValue()) };

				result = new StrategoList(new StrategoTuple(pair, null), result, null);
			}
			return result;
		}
		if (o instanceof FlockLattice) {
			return ((FlockLattice) o).toTerm();
		}
		if (o instanceof FlockValue) {
			return ((FlockValue) o).toTerm();
		}
		if (o instanceof ITerm) {
			return ((ITerm) o).toTerm();
		}
		throw new RuntimeException("Could not identify proper term type of " + o.toString());
	}

	public static IStrategoTerm at(IStrategoTerm term, int index) {
		if (term instanceof IStrategoTuple) {
			IStrategoTuple tuple = (IStrategoTuple) term;
			return tuple.get(index);
		}
		IStrategoAppl appl = (IStrategoAppl) term;
		return appl.getSubterm(index);
	}

	public static Object add(Object l, Object r) {
		l = extractPrimitive(l);
		r = extractPrimitive(r);
		if (l instanceof Integer && r instanceof Integer) {
			return (int) l + (int) r;
		}
		return l.toString() + r.toString();
	}

	public static Object extractPrimitive(Object o) {
		if (o instanceof StrategoInt) {
			StrategoInt term = (StrategoInt) o;
			return term.intValue();
		}
		if (o instanceof StrategoString) {
			StrategoString term = (StrategoString) o;
			return term.stringValue();
		}
		return o;
	}
	
	public static void validateIds(IStrategoTerm n) {
		TermId id = getTermId(n);
		if (id == null) {
			throw new RuntimeException("Null id on term " + n.toString());
		}
		
		for (IStrategoTerm t : n.getAllSubterms()) {
			validateIds(t);
		}
	}

	
	public static TermId getTermId(IStrategoTerm n) {
		if (n.getAnnotations().size() == 0)
			return null;
		assert TermUtils.isAppl(n.getAnnotations().getSubterm(0), "FlockNodeId", 1);
		IStrategoInt id = (IStrategoInt) n.getAnnotations().getSubterm(0).getSubterm(0);
		return new TermId(id.intValue());
	}

	public static IStrategoTerm annotateWithIds(IStrategoTerm t) {
		TermFactory tf = new TermFactory();
		IStrategoList existing = t.getAnnotations();
		IStrategoList newAnnot = tf
				.makeListCons(tf.makeAppl("FlockNodeId", tf.makeInt((int) Flock.nextNodeId().getId())), existing);

		ArrayList<IStrategoTerm> children = new ArrayList<>();
		for (IStrategoTerm subterm : t.getSubterms()) {
			IStrategoTerm subTermAnnotated = annotateWithIds(subterm);
			children.add(subTermAnnotated);
		}
		IStrategoTerm[] childrenArray = children.stream().toArray(IStrategoTerm[]::new);

		if (TermUtils.isAppl(t)) {
			IStrategoAppl asAppl = (IStrategoAppl) t;
			return tf.makeAppl(asAppl.getConstructor(), childrenArray, newAnnot);
		} else if (TermUtils.isList(t)) {
			return tf.makeList(childrenArray, newAnnot);
		} else if (TermUtils.isTuple(t)) {
			return tf.makeTuple(childrenArray, newAnnot);
		} else {
			if (children.size() > 0) {
				throw new RuntimeException("Did not expect this term to have children");
			}
			IStrategoTerm annotated = tf.annotateTerm(t, newAnnot);
			return annotated;
		}
	}
}