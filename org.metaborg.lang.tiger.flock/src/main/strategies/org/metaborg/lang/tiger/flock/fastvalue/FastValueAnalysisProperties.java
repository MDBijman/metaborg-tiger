package org.metaborg.lang.tiger.flock.fastvalue;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.metaborg.lang.tiger.flock.common.FlockValue;
import org.metaborg.lang.tiger.flock.common.Helpers;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.NotImplementedException;

public class FastValueAnalysisProperties {
	static abstract class ConstProp extends FlockValue {
	}

	static class ConstPropConst extends ConstProp {
		int value;

		ConstPropConst(int v) {
			this.value = v;
		}

		@Override
		public int hashCode() {
			return new Integer(value).hashCode();
		}

		@Override
		public IStrategoTerm toTerm() {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm r = factory.makeAppl(factory.makeConstructor("Const", 1),
					new IStrategoTerm[] { factory.makeString(""+this.value) }, null);
			return r;
		}

		@Override
		public void setValue(Object value) {
			this.value = (int) value;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ConstPropConst)) {
				return false;
			}
			ConstPropConst other = (ConstPropConst) obj;
			return this.value == other.value;
		}
	}

	static class ConstPropTop extends ConstProp {
		@Override
		public IStrategoTerm toTerm() {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm r = factory.makeAppl(factory.makeConstructor("Top", 0),
					new IStrategoTerm[] {  }, null);
			return r;
		}

		@Override
		public void setValue(Object o) {
			throw new NotImplementedException();
		}
	}
	
	static class ConstPropBottom extends ConstProp {
		@Override
		public IStrategoTerm toTerm() {
			ITermFactory factory = Flock.instance.factory;
			IStrategoTerm r = factory.makeAppl(factory.makeConstructor("Bottom", 0),
					new IStrategoTerm[] {  }, null);
			return r;
		}

		@Override
		public void setValue(Object o) {
			throw new NotImplementedException();
		}
	}
}