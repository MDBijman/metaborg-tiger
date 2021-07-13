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
	
	public static abstract class FlockValueWithDependencies extends FlockValue {
		Set<Dependency> dependencies = new HashSet<>();
		
		public boolean hasDependency(Dependency d) {
			return dependencies.contains(d);
		}
		public void addDependency(Dependency d) {
			dependencies.add(d);
		}
		public boolean removeDependency(Dependency d) {
			return dependencies.remove(d);
		}
		public Set<Dependency> dependencies() {
			return dependencies;
		}
	}
	
	public static class Name extends FlockValueWithDependencies {
		IStrategoTerm value;
		
		public Name(IStrategoTerm name) {
			super();
			this.value = name;
		}
		
		@Override
		public IStrategoTerm toTerm() {
			return value;
		}
		
		public Name withOrigin(Set<CfgNodeId> ids) {
			for (CfgNodeId id : ids) {
				this.dependencies.add(new Dependency(id));
			}
			return this;
		}
		
		public Name withOrigin(CfgNodeId id) {
			this.dependencies.add(new Dependency(id));
			return this;
		}
	}
}
