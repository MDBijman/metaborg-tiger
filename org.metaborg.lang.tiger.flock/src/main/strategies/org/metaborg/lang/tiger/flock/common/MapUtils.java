package org.metaborg.lang.tiger.flock.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import io.usethesource.capsule.core.trie.*;

public class MapUtils {
	public static Map union(FlockLattice l, FlockLattice r) {
		Map ls = (Map) l.value();
		HashMap rs = (HashMap) r.value();
		Map res = new HashMap();
		res.putAll(ls);
		for (Map.Entry i : (Set<Map.Entry>) rs.entrySet()) {
			if (res.containsKey(i.getKey())) {
				FlockLattice v = (FlockLattice) res.get(i.getKey());
				((FlockLattice) i.getValue()).lub(v);
			} else {
				res.put(i.getKey(), i.getValue());
			}
		}
		return res;
	}

	public static Map union(Map l, Map r) {
		Map ls = (Map) l;
		Map rs = (Map) r;
		Map res = new HashMap();
		res.putAll(ls);
		for (Map.Entry i : (Set<Map.Entry>) rs.entrySet()) {
			if (res.containsKey(i.getKey())) {
				throw new RuntimeException("Key already exists");
			} else {
				res.put(i.getKey(), i.getValue());
			}
		}
		return res;
	}
	
	public static Map union(FlockLattice l, Stream r) {
		return MapUtils.union((Map) (l.value()), r);
	}
	
	public static Map union(Object l, Object r) {
		if (l instanceof FlockLattice && r instanceof FlockLattice) {
			return MapUtils.union((FlockLattice) l, (FlockLattice) r);
		} else if (l instanceof Map && r instanceof Map) {
			return MapUtils.union((Map) l, (Map) r);
		} else if (l instanceof Map && r instanceof Stream) {
			return MapUtils.union((Map) l, (Stream) r);
		} else if (l instanceof FlockLattice && r instanceof Stream) {
			return MapUtils.union((FlockLattice) l, (Stream) r);
		}
		throw new RuntimeException("Cannot union these types");
	}

	/////
	
	public static boolean unionInplace(FlockLattice l, FlockLattice r) {
		boolean changed = false;
		Map ls = (Map) ((FlockLattice) l).value();
		HashMap rs = (HashMap) ((FlockLattice) r).value();
		for (Map.Entry i : (Set<Map.Entry>) rs.entrySet()) {
			if (ls.containsKey(i.getKey())) {
				FlockLattice v = (FlockLattice) ls.get(i.getKey());
				changed |= ((FlockLattice) i.getValue()).lub(v);
			} else {
				changed = true;
				ls.put(i.getKey(), i.getValue());
			}
		}
		return changed;
	}

	public static boolean unionInplace(Map l, Map r) {
		boolean changed = false;
		for (Map.Entry i : (Set<Map.Entry>) r.entrySet()) {
			if (l.containsKey(i.getKey())) {
				throw new RuntimeException("Key already exists");
			} else {
				changed = true;
				l.put(i.getKey(), i.getValue());
			}
		}
		return changed;
	}

	public static boolean unionInplace(Map l, Stream r) {
		boolean changed = false;
		Iterator i = r.iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			Object k = e.getKey();
			Object v = e.getValue();

			if (l.containsKey(k)) {
				changed |= ((FlockLattice) l.get(k)).lub((FlockLattice) v);
			} else {
				l.put(k, v);
				changed = true;
			}
		}
		return changed;
	}
	
	public static boolean unionInplace(FlockLattice l, Stream r) {
		return MapUtils.unionInplace((Map) (l.value()), r);
	}
	
	public static boolean unionInplace(Object l, Object r) {
		// Pretty messy
		if (l instanceof FlockLattice && r instanceof FlockLattice) {
			return MapUtils.unionInplace((FlockLattice) l, (FlockLattice) r);
		} else if (l instanceof Map && r instanceof Map) {
			return MapUtils.unionInplace((Map) l, (Map) r);
		} else if (l instanceof Map && r instanceof Stream) {
			return MapUtils.unionInplace((Map) l, (Stream) r);
		} else if (l instanceof FlockLattice && r instanceof Stream) {
			return MapUtils.unionInplace((FlockLattice) l, (Stream) r);
		}
		throw new RuntimeException("Cannot unionInplace these types");
	}
	
	/////

	public static Stream union(Stream l, Stream r) {
		return Stream.concat(l, r);
	}

	public static Stream create(Stream s) {
		return s;
	}

	public static Map create(Object k, Object v) {
		HashMap result = new HashMap();
		result.put(k, v);
		return result;
	}

	public static Map create() {
		return new HashMap();
	}
}