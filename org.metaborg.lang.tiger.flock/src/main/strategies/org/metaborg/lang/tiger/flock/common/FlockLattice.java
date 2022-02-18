package org.metaborg.lang.tiger.flock.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.StrategoTuple;
import org.spoofax.terms.util.NotImplementedException;

public interface FlockLattice extends Cloneable {
	public abstract boolean lub(Object o);
	
	public abstract FlockLattice copy();

	public abstract Object value();
	
	public abstract void setValue(Object value);
	
	public default boolean leq(FlockLattice r) {
		FlockLattice clone = this.copy();
		clone.lub(r);
		return clone.equals(r);
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
			ITermFactory tf = Flock.instance.factory;
			IStrategoList result = tf.makeList();
			for (Object o : this) {
				result = tf.makeListCons(Helpers.toTerm(o), result);
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
		public FlockLattice copy() {
			return new MustSet(new HashSet<>(this.value));
		}
		
		@Override
		public Object value() {
			return value;
		}
		
		@Override
		public void setValue(Object value) {
			this.value = (Set) value;
		}
		
		@Override
		public boolean lub(Object r) {
			return SetUtils.intersectionInplace(this, r);
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
		public FlockLattice copy() {
			return new MaySet(new HashSet<>(this.value));
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
		public void setValue(Object value) {
			this.value = (Set) value;
		}
		
		@Override
		public boolean lub(Object o) {
			return SetUtils.unionInplace(this, o);
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
			return new SimpleMap(new HashMap<>());
		}
		
		public static SimpleMap top() {
			throw new NotImplementedException();
		}
		
		@Override
		public FlockLattice copy() {
			return new SimpleMap(new HashMap<>(this.value));
		}

		@Override
		public Iterator iterator() {
			return value.entrySet().iterator();
		}
		
		@Override
		public boolean lub(Object o) {
			return MapUtils.unionInplace(this, o);
		}
		
		@Override
		public Object value() {
			return value;
		}
		
		@Override
		public void setValue(Object value) {
			this.value = (Map) value;
		}
		
		@Override
		public IStrategoTerm toTerm() {
		    ITermFactory tf = Flock.instance.factory;

			IStrategoList result = tf.makeList();
			for (Object o : this.value.entrySet()) {
				Entry e = (Entry) o;
				IStrategoTerm[] kids = { Helpers.toTerm(e.getKey()), Helpers.toTerm(e.getValue()) };
				result = tf.makeListCons(tf.makeTuple(kids, null), result);
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