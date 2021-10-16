package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockCollectionLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.FlockValueLattice;
import org.metaborg.lang.tiger.flock.common.FlockLattice.SimpleMap;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MaySet;
import org.metaborg.lang.tiger.flock.common.FlockLattice.MustSet;
import org.metaborg.lang.tiger.flock.common.FlockValue.FlockValueWithDependencies;

import java.util.Set;

public abstract class LatticeDependencyUtils {
	public static boolean removeValuesByDependency(FlockLattice l, Dependency d) {
		Collection valuesInCollection = null;
		if (l instanceof MaySet) {
			valuesInCollection = (Collection) ((MaySet) l).value();
		} else if (l instanceof MustSet) {
			valuesInCollection = (Collection) ((MustSet) l).value();
		} else if (l instanceof SimpleMap) {
			valuesInCollection = ((SimpleMap) l).value.values();
		} else {
			throw new RuntimeException("Unknown lattice instance: " + l.toString());
		}

		return valuesInCollection.removeIf(x -> {
			if (x instanceof FlockCollectionLattice) {
				removeValuesByDependency(l, d);
				return false;
			} else if (x instanceof FlockValueLattice) {
				Object val = ((FlockValueLattice) x).value();
				if (val instanceof FlockValueWithDependencies) {
					return ((FlockValueWithDependencies) val).hasDependency(d);
				} else if (val instanceof FlockValue) {
					return true;
				} else {
					throw new RuntimeException("Unknown lattice instance: " + x.toString());
				}
			}
			
			throw new RuntimeException("Expected lattice or value with dependencies");
		});
	}
	
	public static Set<Dependency> gatherDependencies(FlockLattice l) {
		Set<Dependency> out = new HashSet<>();
		gatherDependencies(out, l);
		return out;
	}

	public static void gatherDependencies(Set<Dependency> out, Object l) {
		if (l instanceof SimpleMap) {
			for (Object o : (SimpleMap) l) {
				Entry e = (Entry) o;
				gatherDependencies(out, (FlockLattice) e.getValue());
			}
		}
		else if (l instanceof FlockCollectionLattice) {
			for (Object o : (FlockCollectionLattice) l) {
				gatherDependencies(out, o);
			}
		} else if (l instanceof FlockValueLattice) {
			if (((FlockValueLattice) l).value() instanceof FlockValueWithDependencies) {
				out.addAll(((FlockValueWithDependencies) ((FlockValueLattice) l).value()).dependencies());
			} else if (((FlockValueLattice) l).value() instanceof FlockValue) {
				return;
			} else {
				throw new RuntimeException("Invalid lattice type");
			}
		} else {
			throw new RuntimeException("Invalid lattice type: " + l.getClass().toString());
		}
	}
	
}