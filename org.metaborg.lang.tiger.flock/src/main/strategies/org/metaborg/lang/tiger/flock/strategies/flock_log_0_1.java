package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_log_0_1 extends Strategy {

	public static flock_log_0_1 instance = new flock_log_0_1();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm tag) {
		if (!TermUtils.isString(tag)) {
			return null;
		}
		
		Flock.log(M.string(tag), current.toString());
		return current;
	}
}