package org.metaborg.lang.tiger.flock.common;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.HashSet;

import org.metaborg.lang.tiger.flock.common.FlockValue.Name;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class FlockValue {
	public abstract IStrategoTerm toTerm();
	
	public abstract void setValue(Object o);
	
	public static class Name extends FlockValue {
		IStrategoTerm value;
		
		public Name(IStrategoTerm name) {
			super();
			this.value = name;
		}
		
		@Override
		public IStrategoTerm toTerm() {
			return value;
		}

		@Override
		public void setValue(Object value) {
			this.value = (IStrategoTerm) value;
		}
	}
}
