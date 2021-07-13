package org.metaborg.lang.tiger.flock.value;

import java.util.Set;

import org.metaborg.lang.tiger.flock.common.CfgNodeId;
import org.metaborg.lang.tiger.flock.common.Dependency;
import org.metaborg.lang.tiger.flock.common.FlockValue.FlockValueWithDependencies;
import org.metaborg.lang.tiger.flock.common.SetUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class FlowAnalysisProperties {
	static class ConstProp extends FlockValueWithDependencies {
		public IStrategoTerm value;
		public Set<Dependency> dependencies;

		ConstProp(ConstProp other) {
			this.value = other.value;
			this.dependencies = other.dependencies;
		}

		ConstProp(IStrategoTerm value) {
			this.value = value;
			this.dependencies = SetUtils.create();
		}

		ConstProp(IStrategoTerm value, Dependency origin) {
			this.value = value;
			this.dependencies = SetUtils.create(origin);
		}

		ConstProp(IStrategoTerm value, Set<Dependency> dependencies) {
			this.value = value;
			this.dependencies.addAll(dependencies);
		}

		public ConstProp withOrigin(Set<Dependency> dependencies) {
			this.dependencies.addAll(dependencies);
			return this;
		}

		public ConstProp withOrigin(CfgNodeId id) {
			this.dependencies.add(new Dependency(id));
			return this;
		}

		@Override
		public IStrategoTerm toTerm() {
			return this.value;
		}

		@Override
		public String toString() {
			String pre = "Value(\"" + this.value.toString() + "\", {";
			StringBuilder sb = new StringBuilder();
			for (Dependency dep : dependencies) {
				sb.append(dep.id.getId());
				sb.append(",");
			}
			String post = "})";
			return pre + sb.toString() + post;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ConstProp)) {
				return false;
			}
			ConstProp other = (ConstProp) obj;
			return this.value.equals(other.value) && this.dependencies.equals(other.dependencies);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public Set<Dependency> dependencies() {
			return this.dependencies;
		}

		@Override
		public boolean removeDependency(Dependency d) {
			return this.dependencies.remove(d);
		}

		@Override
		public boolean hasDependency(Dependency d) {
			return this.dependencies.contains(d);
		}

		@Override
		public void addDependency(Dependency d) {
			this.dependencies.add(d);
		}
	}
}