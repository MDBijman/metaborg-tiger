package org.metaborg.lang.tiger.flock.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockLatticeInPlace;

public class MapUtils {
	public static Map union(Object l, Object r) {
		if (l instanceof FlockLattice && r instanceof FlockLattice) {
			Map ls = (Map) ((FlockLattice) l).value();
			HashMap rs = (HashMap) ((FlockLattice) r).value();
			Map res = new HashMap();
			res.putAll(ls);
			for (Map.Entry i : (Set<Map.Entry>) rs.entrySet()) {
				if (res.containsKey(i.getKey())) {
					FlockLattice v = (FlockLattice) res.get(i.getKey());
					FlockLattice m = ((FlockLattice) i.getValue()).lub(v);
					res.put(i.getKey(), m);
				} else {
					res.put(i.getKey(), i.getValue());
				}
			}
			return res;
		} else if (l instanceof Map && r instanceof Map) {
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
		} else {
			throw new RuntimeException("Cannot union these types");
		}
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