package org.metaborg.lang.tiger.flock.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.lang.tiger.flock.common.TermTree.ITerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.NotImplementedException;

public interface FlockLattice {
	public abstract FlockLattice lub(FlockLattice o);

	public abstract Object value();
	
	public default boolean leq(FlockLattice r) {
		return this.lub(r).equals(r);
	}

	public default boolean nleq(FlockLattice r) {
		return !this.leq(r);
	}

	public default Object glb(FlockLattice r) {
		return null;
	}

	public default boolean geq(FlockLattice r) {
		return this.glb(r).equals(this);
	}
	
	public abstract IStrategoTerm toTerm();
	
	/*
	 * Default Implementations
	 */
	
	public static interface FlockCollectionLattice extends FlockLattice, Iterable {
		@Override
		public default IStrategoTerm toTerm() {
			IStrategoList result = new StrategoList(null, null, null);
			for (Object o : this) {
				result = new StrategoList(Helpers.toTerm(o), result, null);
			}
			return result;
		}
	}
	public static interface FlockValueLattice extends FlockLattice {
		public abstract FlockValue value();
		
		@Override
		public default IStrategoTerm toTerm() {
			return this.value().toTerm();
		}
	}
	
	public static class MustSet implements FlockCollectionLattice {
		Set value;
		
		public MustSet(MustSet o) {
			this.value = new HashSet<>();
			this.value.addAll(o.value);
		}
		
		public MustSet(Set v) {
			this.value = v;
		}
		
		public static MustSet bottom() {
			return new MustSet(new UniversalSet());
		}

		public static MustSet top() {
			return new MustSet(new HashSet());
		}
		
		@Override
		public Object value() {
			return value;
		}

		@Override
		public FlockLattice lub(FlockLattice r) {
			return new MustSet(SetUtils.intersection(this.value(), r.value()));
		}

		@Override
		public boolean leq(FlockLattice r) {
			return SetUtils.isSupersetEquals(this.value(), r.value());
		}

		@Override
		public FlockLattice glb(FlockLattice r) {
			return new MustSet(SetUtils.union(this.value(), r.value()));
		}

		@Override
		public boolean geq(FlockLattice r) {
			return SetUtils.isSubsetEquals(this.value(), r.value());
		}

		@Override
		public Iterator iterator() {
			return value.iterator();
		}

		@Override
		public IStrategoTerm toTerm() {
			IStrategoList result = new StrategoList(null, null, null);
			for (Object o : this.value) {
				result = new StrategoList(Helpers.toTerm(o), result, null);
			}
			return result;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			MaySet rhs = (MaySet) other;
			return this.value.equals(rhs.value);
		}
	}

	public static class MaySet implements FlockCollectionLattice {
		Set value;
		
		public MaySet(MaySet v) {
			this.value = new HashSet<>();
			this.value.addAll(v.value);
		}
		
		public MaySet(Set v) {
			this.value = v;
		}
		
		public static MaySet bottom() {
			return new MaySet(new HashSet());
		}

		public static MaySet top() {
			return new MaySet(new UniversalSet());
		}

		@Override
		public String toString() {
			return value.toString();
		}
		
		@Override
		public Object value() {
			return value;
		}

		@Override
		public FlockLattice lub(FlockLattice r) {
			return new MaySet(SetUtils.union(this.value(), r.value()));
		}

		@Override
		public boolean leq(FlockLattice r) {
			return SetUtils.isSubsetEquals(this.value(), r.value());
		}

		@Override
		public FlockLattice glb(FlockLattice r) {
			return new MaySet(SetUtils.intersection(this.value(), r.value()));
		}

		@Override
		public boolean geq(FlockLattice r) {
			return SetUtils.isSupersetEquals(this.value(), r.value());
		}
	
		@Override
		public Iterator iterator() {
			return value.iterator();
		}

		@Override
		public IStrategoTerm toTerm() {
			IStrategoList result = new StrategoList(null, null, null);
			for (Object o : this.value) {
				result = new StrategoList(Helpers.toTerm(o), result, null);
			}
			return result;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			MaySet rhs = (MaySet) other;
			return this.value.equals(rhs.value);
		}
	}

	public static class SimpleMap implements FlockCollectionLattice {
		Map value;

		public SimpleMap(FlockLattice o) {
			this.value = ((SimpleMap) o).value;
		}
		
		public SimpleMap(SimpleMap o) {
			this.value = o.value;
		}
		
		public SimpleMap(Map v) {
			this.value = v;
		}

		public static SimpleMap bottom() {
			return new SimpleMap(new HashMap());
		}
		
		public static SimpleMap top() {
			throw new NotImplementedException();
		}

		@Override
		public Iterator iterator() {
			return value.entrySet().iterator();
		}
		
		@Override
		public FlockLattice lub(FlockLattice o) {
			return new SimpleMap(MapUtils.union(this, o));
		}

		@Override
		public Object value() {
			return value;
		}
		
		@Override
		public IStrategoTerm toTerm() {
			IStrategoList result = new StrategoList(null, null, null);
			for (Object o : this.value.entrySet()) {
				Entry e = (Entry) o;
				IStrategoTerm[] kids = { Helpers.toTerm(e.getKey()), Helpers.toTerm(e.getValue()) };
				result = new StrategoList(new StrategoTuple(kids, null), result, null);
			}
			return result;
		}
		
		@Override
		public String toString() {
			return value.toString();
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			SimpleMap rhs = (SimpleMap) other;
			return this.value.equals(rhs.value);
		}
	}
}