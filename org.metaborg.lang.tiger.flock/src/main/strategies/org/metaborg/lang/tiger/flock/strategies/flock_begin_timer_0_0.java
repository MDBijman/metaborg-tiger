package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.common.Flock;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.M;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flock_begin_timer_0_0 extends Strategy {

	public static flock_begin_timer_0_0 instance = new flock_begin_timer_0_0();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm program) {
		if (!TermUtils.isString(program)) {
			return null;
		}
		
		Flock.beginTime(TermUtils.toJavaString(program));
		return program;
	}
}