package org.metaborg.lang.tiger.flock.value;

import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class FlowAnalysisProperties {
	static class ConstProp extends FlockValue {
		public IStrategoTerm value;

		ConstProp(IStrategoTerm value) {
			this.value = value;
		}

		@Override
		public String toString() {
			String pre = "Value(\"" + this.value.toString() + "\", {";
			StringBuilder sb = new StringBuilder();
			String post = "})";
			return pre + sb.toString() + post;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (other.getClass() != this.getClass())
				return false;
			ConstProp rhs = (ConstProp) other;
			return this.value.equals(rhs.value);
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