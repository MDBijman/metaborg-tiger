package org.metaborg.lang.tiger.flock.experiments.singlevalue;

import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SingleValueAnalysisProperties {
	static class ConstProp extends FlockValue {
		public IStrategoTerm value;

		ConstProp(IStrategoTerm value) {
			this.value = value;
		}

		@Override
		public void setValue(Object value) {
			this.value = (IStrategoTerm) value;
		}

		@Override
		public String toString() {
			String pre = "Value(\"" + this.value.toString() + "\", {";
			StringBuilder sb = new StringBuilder();
			String post = "})";
			return pre + sb.toString() + post;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ConstProp)) {
				return false;
			}
			ConstProp other = (ConstProp) obj;
			return this.value.equals(other.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public IStrategoTerm toTerm() {
			return this.value;
		}
	}
}