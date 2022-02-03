package org.metaborg.lang.tiger.flock.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class SetUtils {
	public static Set create(Object... terms) {
		Set result = new HashSet();
		for (Object term : terms) {
			result.add(term);
		}
		return result;
	}

	public static Set union(Object o1, Object o2) {
		Set l = (Set) o1;
		Set r = (Set) o2;
		if (l instanceof UniversalSet || r instanceof UniversalSet) {
			return new UniversalSet();
		}
		Set<IStrategoTerm> result = new HashSet();
		result.addAll(l);
		result.addAll(r);
		return result;
	}
	
	public static boolean intersectionInplace(Object l, Object r) {
		Set ls;
		if (l instanceof FlockLattice) {
			ls = (Set) ((FlockLattice) l).value();
		} else {
			ls = (Set) l;
		}
		
		Set rs;
		if (r instanceof FlockLattice) {
			rs = (Set) ((FlockLattice) r).value();
		} else {
			rs = (Set) r;
		}

		if (ls instanceof UniversalSet) {
			throw new RuntimeException("Cannot perform in place intersect for lhs universal set");
		}
		if (rs instanceof UniversalSet) {
			return false;
		}
		
		return ls.retainAll(rs);
	}

	
	public static boolean unionInplace(Object l, Object r) {
		Set ls;
		if (l instanceof FlockLattice) {
			ls = (Set) ((FlockLattice) l).value();
		} else {
			ls = (Set) l;
		}
		
		Set rs;
		if (r instanceof FlockLattice) {
			rs = (Set) ((FlockLattice) r).value();
		} else {
			rs = (Set) r;
		}
		
		if (rs instanceof UniversalSet) {
			throw new RuntimeException("Cannot perform in place union for rhs universal set");
		}
		if (ls instanceof UniversalSet) {
			return false;
		}
		return ls.addAll(rs);
	}

	public static Set intersection(Object l, Object r) {
		Set ls = (Set) l;
		Set rs = (Set) r;
		if (ls instanceof UniversalSet) {
			return rs;
		}
		if (rs instanceof UniversalSet) {
			return ls;
		}
		Set result = new HashSet();
		for (Object i : ls) {
			if (rs.contains(i)) {
				result.add(i);
			}
		}
		return result;
	}

	public static Set difference(Object l, Object r) {
		Set ls = (Set) l;
		Set rs = (Set) r;
		Set result = new HashSet();
		for (Object i : ls) {
			if (!rs.contains(i)) {
				result.add(i);
			}
		}
		return result;
	}

	public static boolean isSubsetEquals(Object l, Object r) {
		Set ls = (Set) l;
		Set rs = (Set) r;
		if (ls instanceof UniversalSet) {
			return false;
		}
		if (rs instanceof UniversalSet) {
			return true;
		}
		for (Object i : ls) {
			if (!rs.contains(i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isSupersetEquals(Object l, Object r) {
		Set ls = (Set) l;
		Set rs = (Set) r;
		if (ls instanceof UniversalSet) {
			return true;
		}
		if (rs instanceof UniversalSet) {
			return false;
		}
		for (Object i : rs) {
			if (!ls.contains(i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isSuperset(Object l, Object r) {
		if (l.equals(r)) {
			return false;
		}
		return isSupersetEquals(l, r);
	}

	public static boolean isSubset(Object l, Object r) {
		if (l.equals(r)) {
			return false;
		}
		return isSubsetEquals(l, r);
	}
}